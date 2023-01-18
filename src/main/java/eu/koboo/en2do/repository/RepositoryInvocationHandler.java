package eu.koboo.en2do.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import eu.koboo.en2do.exception.RepositoryInvalidCallException;
import eu.koboo.en2do.repository.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.repository.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.methods.options.transform.Transform;
import eu.koboo.en2do.repository.methods.options.sort.annotation.Limit;
import eu.koboo.en2do.repository.methods.options.sort.annotation.Skip;
import eu.koboo.en2do.repository.methods.options.sort.annotation.SortBy;
import eu.koboo.en2do.repository.methods.options.sort.parameter.Sort;
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
                findIterable = applySortObject(method, findIterable, arguments);
                findIterable = applySortAnnotations(method, findIterable);
                return findIterable.limit(1).first();
            }
            case FIND_MANY -> {
                FindIterable<E> findIterable = collection.find(filter);
                if (repositoryMeta.isAppendMethodAsComment()) {
                    findIterable.comment("en2do \"" + methodName + "\"");
                }
                findIterable = applySortObject(method, findIterable, arguments);
                findIterable = applySortAnnotations(method, findIterable);
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

    private FindIterable<E> applySortObject(Method method, FindIterable<E> findIterable, Object[] args) {
        int parameterCount = method.getParameterCount();
        if (parameterCount <= 0) {
            return findIterable;
        }
        Class<?> lastParamType = method.getParameterTypes()[method.getParameterCount() - 1];
        if (!lastParamType.isAssignableFrom(Sort.class)) {
            return findIterable;
        }
        Object lastParamObject = args == null ? null : args[args.length - 1];
        if (!(lastParamObject instanceof Sort sortOptions)) {
            return findIterable;
        }
        if (!sortOptions.getFieldDirectionMap().isEmpty()) {
            for (Map.Entry<String, Integer> byField : sortOptions.getFieldDirectionMap().entrySet()) {
                findIterable = findIterable.sort(new BasicDBObject(byField.getKey(), byField.getValue()));
            }
        }
        if (sortOptions.getLimit() != -1) {
            findIterable = findIterable.limit(sortOptions.getLimit());
        }
        if (sortOptions.getSkip() != -1) {
            findIterable = findIterable.skip(sortOptions.getSkip());
        }
        findIterable.allowDiskUse(true);
        return findIterable;
    }

    private FindIterable<E> applySortAnnotations(Method method, FindIterable<E> findIterable) {
        SortBy[] sortAnnotations = method.getAnnotationsByType(SortBy.class);
        if (sortAnnotations != null) {
            for (SortBy sortBy : sortAnnotations) {
                int orderType = sortBy.ascending() ? 1 : -1;
                findIterable = findIterable.sort(new BasicDBObject(sortBy.field(), orderType));
            }
        }
        if (method.isAnnotationPresent(Limit.class)) {
            Limit limit = method.getAnnotation(Limit.class);
            findIterable = findIterable.limit(limit.value());
        }
        if (method.isAnnotationPresent(Skip.class)) {
            Skip skip = method.getAnnotation(Skip.class);
            findIterable = findIterable.skip(skip.value());
        }
        findIterable.allowDiskUse(true);
        return findIterable;
    }
}
