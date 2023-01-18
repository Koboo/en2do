package eu.koboo.en2do.repository.internal;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.exception.RepositoryInvalidCallException;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.internal.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.repository.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.options.methods.transform.Transform;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class RepositoryInvocationHandler<E, ID, R extends Repository<E, ID>> implements InvocationHandler {

    RepositoryMeta<E, ID, R> repositoryMeta;
    MongoCollection<E> collection;

    @Override
    @SuppressWarnings("all")
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        String methodName = method.getName();

        Transform transform = method.getAnnotation(Transform.class);
        if (transform != null) {
            methodName = transform.value();
        }

        // Get and check if a static handler for the methodName is available.
        PredefinedMethod<E, ID, R> methodHandler = repositoryMeta.lookupPredefinedMethod(methodName);
        if (methodHandler != null) {
            // Just handle the arguments and return the object
            return methodHandler.handle(method, arguments);
        }
        // No static handler found.

        // Get and check if any dynamic method matches the methodName
        DynamicMethod<E, ID, R> dynamicMethod = repositoryMeta.lookupDynamicMethod(methodName);
        if (dynamicMethod == null) {
            // No handling found for method with this name.
            throw new RepositoryInvalidCallException(method, repositoryMeta.getRepositoryClass());
        }

        // Generate bson filter by dynamic Method object.
        Bson filter = dynamicMethod.createBsonFilter(arguments);
        // Switch-case the method operator to use the correct mongo query.
        return switch (dynamicMethod.getMethodOperator()) {
            case COUNT -> repositoryMeta.getCollection().countDocuments(filter);
            case DELETE -> repositoryMeta.getCollection().deleteMany(filter).wasAcknowledged();
            case EXISTS -> repositoryMeta.getCollection().countDocuments(filter) > 0;
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
            default -> // Couldn't find any match method operator
                    throw new RepositoryInvalidCallException(method, repositoryMeta.getRepositoryClass());
        };
    }
}
