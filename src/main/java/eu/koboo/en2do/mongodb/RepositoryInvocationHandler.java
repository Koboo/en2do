package eu.koboo.en2do.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.mongodb.exception.methods.MethodUnsupportedException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryInvalidCallException;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class RepositoryInvocationHandler<E, ID, R extends Repository<E, ID>> implements InvocationHandler {

    RepositoryData<E, ID, R> repositoryData;
    ExecutorService executorService;
    Map<String, GlobalPredefinedMethod> predefinedMethodRegistry;

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

        // Get and check if a static handler for the methodName is available.
        GlobalPredefinedMethod methodHandler = predefinedMethodRegistry.get(methodName);
        if (methodHandler != null) {
            // Just handle the arguments and return the object
            return methodHandler.handle(repositoryData, method, arguments);
        }

        // Check for predefined method with async prefix.
        boolean isAsyncMethod = method.isAnnotationPresent(Async.class);
        if (transform == null && isAsyncMethod) {
            String predefinedName = repositoryData.stripAsyncName(methodName);
            methodHandler = predefinedMethodRegistry.get(predefinedName);
            if (methodHandler != null) {
                // Just handle the arguments and return the object
                CompletableFuture<Object> future = new CompletableFuture<>();
                GlobalPredefinedMethod finalMethodHandler = methodHandler;
                executeFuture(future, () -> finalMethodHandler.handle(repositoryData, method, arguments));
                return future;
            }
        }

        // No predefined handler found, checking for dynamic methods

        // Get and check if any dynamic method matches the methodName
        IndexedMethod<E, ID, R> dynamicMethod = repositoryData.lookupDynamicMethod(methodName);
        if (dynamicMethod == null) {
            // No handling found for method with this name.
            throw new MethodUnsupportedException(method, repositoryData.getRepositoryClass());
        }

        MethodCallable methodCallable = () -> executeMethod(dynamicMethod, arguments, method, methodName);
        if (isAsyncMethod) {
            CompletableFuture<Object> future = new CompletableFuture<>();
            executeFuture(future, methodCallable);
            return future;
        } else {
            return methodCallable.call();
        }
    }

    private Object executeMethod(IndexedMethod<E, ID, R> indexedMethod, Object[] arguments, Method method, String methodName) throws Exception {
        // Generate bson filter by dynamic Method object.
        Bson filter = indexedMethod.createFilter(arguments);

        if (filter == null) {
            throw new NullPointerException("The created filter for " + indexedMethod.getMethod().getName() + " is null!");
        }

        // Switch-case the method operator to use the correct mongo query.
        final MongoCollection<E> collection = repositoryData.getEntityCollection();

        FindIterable<E> findIterable;
        switch (indexedMethod.getMethodOperator()) {
            case COUNT:
                return collection.countDocuments(filter);
            case DELETE:
                return collection.deleteMany(filter).wasAcknowledged();
            case EXISTS:
                return collection.countDocuments(filter) > 0;
            case FIND:
                findIterable = repositoryData.createIterable(filter, methodName);
                findIterable = repositoryData.applySortObject(method, findIterable, arguments);
                findIterable = repositoryData.applySortAnnotations(method, findIterable);

                // Because it's a find method, we always got an entity defined count.
                // "Many" = -1 / unlimited
                // "Top" = user specific count
                // "First" = 1 / first entity
                Long methodDefinedEntityCount = indexedMethod.getMethodDefinedEntityCount();
                if (methodDefinedEntityCount == -1 || methodDefinedEntityCount > 1) {
                    if (methodDefinedEntityCount != -1) {
                        findIterable = findIterable.limit(Math.toIntExact(methodDefinedEntityCount));
                    }
                    return findIterable.into(new ArrayList<>());
                } else {
                    return findIterable.first();
                }
            case PAGE:
                findIterable = repositoryData.createIterable(filter, methodName);
                findIterable = repositoryData.applyPageObject(method, findIterable, arguments);
                return findIterable.into(new ArrayList<>());
            case UPDATE_FIELD:
                UpdateBatch updateBatch = (UpdateBatch) arguments[arguments.length - 1];
                UpdateResult result = collection.updateMany(filter, repositoryData.createUpdateDocument(updateBatch),
                    new UpdateOptions().upsert(false));
                return result.wasAcknowledged();
            default:
                // Couldn't find any match method operator, but that shouldn't happen
                throw new RepositoryInvalidCallException(method, repositoryData.getRepositoryClass());
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
