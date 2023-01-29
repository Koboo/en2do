package eu.koboo.en2do.internal;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.internal.exception.methods.MethodUnsupportedException;
import eu.koboo.en2do.internal.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.transform.Transform;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class RepositoryInvocationHandler<E, ID, R extends Repository<E, ID>> implements InvocationHandler {

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

        // Get and check if a static handler for the methodName is available.
        PredefinedMethod<E, ID, R> methodHandler = repositoryMeta.lookupPredefinedMethod(methodName);
        if (methodHandler != null) {
            // Just handle the arguments and return the object
            return methodHandler.handle(method, arguments);
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
                executeFuture(future, () -> methodHandlerFuture.handle(method, arguments));
                return future;
            }
        }

        // Get and check if any dynamic method matches the methodName
        DynamicMethod<E, ID, R> dynamicMethod = repositoryMeta.lookupDynamicMethod(methodName);
        if (dynamicMethod == null) {
            // No handling found for method with this name.
            throw new MethodUnsupportedException(method, repositoryMeta.getRepositoryClass());
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

    private Object executeMethod(DynamicMethod<E, ID, R> dynamicMethod, Object[] arguments, Method method, String methodName) throws Exception {
        // Generate bson filter by dynamic Method object.
        Bson filter = dynamicMethod.createBsonFilter(arguments);
        // Switch-case the method operator to use the correct mongo query.
        final MongoCollection<E> collection = repositoryMeta.getCollection();

        return switch (dynamicMethod.getMethodOperator()) {
            case COUNT -> collection.countDocuments(filter);
            case DELETE -> collection.deleteMany(filter).wasAcknowledged();
            case EXISTS -> collection.countDocuments(filter) > 0;
            case FIND_MANY -> {
                FindIterable<E> findIterable = repositoryMeta.createIterable(filter, methodName);
                findIterable = repositoryMeta.applySortObject(method, findIterable, arguments);
                findIterable = repositoryMeta.applySortAnnotations(method, findIterable);
                yield findIterable.into(new ArrayList<>());
            }
            case FIND_FIRST -> {
                FindIterable<E> findIterable = repositoryMeta.createIterable(filter, methodName);
                findIterable = repositoryMeta.applySortObject(method, findIterable, arguments);
                findIterable = repositoryMeta.applySortAnnotations(method, findIterable);
                yield findIterable.limit(1).first();
            }
            case PAGE -> {
                FindIterable<E> findIterable = repositoryMeta.createIterable(filter, methodName);
                findIterable = repositoryMeta.applyPageObject(method, findIterable, arguments);
                yield findIterable.into(new ArrayList<>());
            }
            // Couldn't find any match method operator, but that shouldn't happen
        };
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
