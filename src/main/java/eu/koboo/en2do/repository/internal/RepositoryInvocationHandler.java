package eu.koboo.en2do.repository.internal;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.exception.RepositoryInvalidCallException;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.internal.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.repository.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.options.methods.transform.Transform;
import eu.koboo.en2do.repository.options.methods.sort.Limit;
import eu.koboo.en2do.repository.options.methods.sort.Skip;
import eu.koboo.en2do.repository.options.methods.sort.SortBy;
import eu.koboo.en2do.repository.options.methods.sort.Sort;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

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
        switch (dynamicMethod.getMethodOperator()) {
            case FIND_FIRST -> {
                FindIterable<E> findIterable = collection.find(filter);
                if (repositoryMeta.isAppendMethodAsComment()) {
                    findIterable.comment("en2do \"" + methodName + "\"");
                }
                findIterable = repositoryMeta.applySortObject(method, findIterable, arguments);
                findIterable = repositoryMeta.applySortAnnotations(method, findIterable);
                return findIterable.limit(1).first();
            }
            case FIND_MANY -> {
                FindIterable<E> findIterable = collection.find(filter);
                if (repositoryMeta.isAppendMethodAsComment()) {
                    findIterable.comment("en2do \"" + methodName + "\"");
                }
                findIterable = repositoryMeta.applySortObject(method, findIterable, arguments);
                findIterable = repositoryMeta.applySortAnnotations(method, findIterable);
                return findIterable.into(new ArrayList<>());
            }
            case DELETE -> {
                DeleteResult deleteResult = collection.deleteMany(filter);
                return deleteResult.wasAcknowledged();
            }
            case EXISTS -> {
                return collection.countDocuments(filter) > 0;
            }
            case COUNT -> {
                return collection.countDocuments(filter);
            }
        }
        throw new RepositoryInvalidCallException(method, repositoryMeta.getRepositoryClass());
    }
}
