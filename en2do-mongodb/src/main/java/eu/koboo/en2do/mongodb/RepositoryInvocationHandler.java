package eu.koboo.en2do.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.cache.Cache;
import eu.koboo.en2do.cache.CacheGet;
import eu.koboo.en2do.cache.CachePut;
import eu.koboo.en2do.cache.CacheRemove;
import eu.koboo.en2do.mongodb.exception.methods.MethodUnsupportedException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryInvalidCallException;
import eu.koboo.en2do.mongodb.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.transform.Transform;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class RepositoryInvocationHandler<E, ID, R extends Repository<E, ID>> implements InvocationHandler {

    MongoManager mongoManager;
    RepositoryMeta<E, ID, R> repositoryMeta;
    ExecutorService executorService;

    @Override
    @SuppressWarnings("all")
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {

        // Create value of the final methodName
        String tempMethodName = method.getName();
        Transform transform = method.getAnnotation(Transform.class);
        if (transform != null) {
            tempMethodName = transform.value();
        }
        String methodName = tempMethodName;

        List<Cache> cacheList = mongoManager.getAllCaches(repositoryMeta.getRepositoryClass());
        CacheGet cacheGet = method.getAnnotation(CacheGet.class);
        if (cacheGet != null
            && arguments != null
            && arguments.length > 0
            && cacheList != null
            && !cacheList.isEmpty()) {
            Object cacheHit = null;
            for (Cache cache : cacheList) {
                cacheHit = cache.getObject(cacheGet, arguments);
                if (cacheHit == null) {
                    continue;
                }
                return cacheHit;
            }
        }

        // Get and check if a static handler for the methodName is available.
        PredefinedMethod<E, ID, R> methodHandler = repositoryMeta.lookupPredefinedMethod(methodName);
        if (methodHandler != null) {
            // Just handle the arguments and return the object
            Object result = methodHandler.handle(method, arguments);
            handleCaching(method, cacheList, arguments, result);
            return result;
        }
        // No static handler found.

        // Check for predefined method with async prefix.
        boolean isAsyncMethod = method.isAnnotationPresent(Async.class);
        if (transform == null && isAsyncMethod) {
            String predefinedName = repositoryMeta.getPredefinedNameByAsyncName(methodName);
            PredefinedMethod<E, ID, R> methodHandlerFuture = repositoryMeta.lookupPredefinedMethod(predefinedName);
            if (methodHandlerFuture != null) {
                // Just handle the arguments and return the object
                CompletableFuture<Object> future = new CompletableFuture<>();
                executeFuture(future, () -> {
                    Object result = methodHandlerFuture.handle(method, arguments);
                    handleCaching(method, cacheList, arguments, result);
                    return result;
                });
                return future;
            }
        }

        // Get and check if any dynamic method matches the methodName
        DynamicMethod<E, ID, R> dynamicMethod = repositoryMeta.lookupDynamicMethod(methodName);
        if (dynamicMethod == null) {
            // No handling found for method with this name.
            throw new MethodUnsupportedException(method, repositoryMeta.getRepositoryClass());
        }

        MethodCallable methodCallable = () -> {
            Object result = executeMethod(dynamicMethod, arguments, method, methodName);
            handleCaching(method, cacheList, arguments, result);
            return result;
        };
        if (isAsyncMethod) {
            CompletableFuture<Object> future = new CompletableFuture<>();
            executeFuture(future, methodCallable);
            return future;
        } else {
            return methodCallable.call();
        }
    }

    private void handleCaching(Method method, List<Cache> cacheList, Object[] arguments, Object result) {
        CacheRemove cacheRemove = method.getAnnotation(CacheRemove.class);
        if(cacheRemove != null) {
            if (arguments == null || arguments.length == 0 || cacheList == null || cacheList.isEmpty()) {
                return;
            }
            if (method.isAnnotationPresent(Async.class)) {
                removeFromCache(cacheRemove, cacheList, arguments);
            } else {
                executorService.execute(() -> removeFromCache(cacheRemove, cacheList, arguments));
            }
        }
        CachePut cachePut = method.getAnnotation(CachePut.class);
        if (cachePut != null) {
            if (arguments == null || arguments.length == 0 || cacheList == null || cacheList.isEmpty()) {
                return;
            }
            if (method.isAnnotationPresent(Async.class)) {
                addToCache(cachePut, cacheList, arguments, result);
            } else {
                executorService.execute(() -> addToCache(cachePut, cacheList, arguments, result));
            }
        }
    }

    private void addToCache(CachePut cachePut, List<Cache> cacheList, Object[] arguments, Object result) {
        for (Cache cache : cacheList) {
            cache.addObject(cachePut, arguments, result);
        }
    }

    private void removeFromCache(CacheRemove cachePut, List<Cache> cacheList, Object[] arguments) {
        for (Cache cache : cacheList) {
            cache.removeObject(cachePut, arguments);
        }
    }

    private Object executeMethod(DynamicMethod<E, ID, R> dynamicMethod, Object[] arguments, Method method, String methodName) throws Exception {
        // Generate bson filter by dynamic Method object.
        Bson filter = dynamicMethod.createFilter(arguments);
        // Switch-case the method operator to use the correct mongo query.
        final MongoCollection<E> collection = repositoryMeta.getCollection();

        FindIterable<E> findIterable;
        switch (dynamicMethod.getMethodOperator()) {
            case COUNT:
                return collection.countDocuments(filter);
            case DELETE:
                return collection.deleteMany(filter).wasAcknowledged();
            case EXISTS:
                return collection.countDocuments(filter) > 0;
            case FIND_MANY:
                findIterable = repositoryMeta.createIterable(filter, methodName);
                findIterable = repositoryMeta.applySortObject(method, findIterable, arguments);
                findIterable = repositoryMeta.applySortAnnotations(method, findIterable);
                return findIterable.into(new ArrayList<>());
            case FIND_FIRST:
                findIterable = repositoryMeta.createIterable(filter, methodName);
                findIterable = repositoryMeta.applySortObject(method, findIterable, arguments);
                findIterable = repositoryMeta.applySortAnnotations(method, findIterable);
                return findIterable.limit(1).first();
            case PAGE:
                findIterable = repositoryMeta.createIterable(filter, methodName);
                findIterable = repositoryMeta.applyPageObject(method, findIterable, arguments);
                return findIterable.into(new ArrayList<>());
            case UPDATE_FIELD:
                UpdateBatch updateBatch = (UpdateBatch) arguments[arguments.length - 1];
                UpdateResult result = collection.updateMany(filter, repositoryMeta.createUpdateDocument(updateBatch),
                    new UpdateOptions().upsert(false));
                return result.wasAcknowledged();
            default:
                // Couldn't find any match method operator, but that shouldn't happen
                throw new RepositoryInvalidCallException(method, repositoryMeta.getRepositoryClass());
        }
    }

    private void executeFuture(CompletableFuture<Object> future, MethodCallable callable) {
        future.completeAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService == null ? future.defaultExecutor() : executorService);
    }
}
