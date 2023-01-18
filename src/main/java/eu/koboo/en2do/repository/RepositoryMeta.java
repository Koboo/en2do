package eu.koboo.en2do.repository;

import com.mongodb.client.model.Filters;
import eu.koboo.en2do.Repository;
import eu.koboo.en2do.repository.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.repository.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.options.AppendMethodAsComment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RepositoryMeta<E, ID, R extends Repository<E, ID>> {

    @Getter
    String collectionName;

    @Getter
    Class<R> repositoryClass;
    @Getter
    Class<E> entityClass;

    @Getter
    Set<Field> entityFieldSet;

    @Getter
    Class<ID> entityUniqueIdClass;
    @Getter
    Field entityUniqueIdField;

    @Getter
    boolean appendMethodAsComment;

    Map<String, PredefinedMethod<E, ID, R>> methodRegistry;
    Map<String, DynamicMethod<E, ID, R>> dynamicMethodRegistry;

    public RepositoryMeta(Class<R> repositoryClass, Class<E> entityClass, Set<Field> entityFieldSet,
                          Class<ID> entityUniqueIdClass, Field entityUniqueIdField, String collectionName) {
        this.collectionName = collectionName;

        this.repositoryClass = repositoryClass;
        this.entityClass = entityClass;

        this.entityFieldSet = entityFieldSet;

        this.entityUniqueIdClass = entityUniqueIdClass;
        this.entityUniqueIdField = entityUniqueIdField;

        this.appendMethodAsComment = repositoryClass.isAnnotationPresent(AppendMethodAsComment.class);

        this.methodRegistry = new HashMap<>();
        this.dynamicMethodRegistry = new HashMap<>();
    }

    public void destroy() {
        methodRegistry.clear();
        dynamicMethodRegistry.clear();
    }

    public boolean isRepositoryMethod(String methodName) {
        return methodRegistry.containsKey(methodName);
    }

    public void registerPredefinedMethod(PredefinedMethod<E, ID, R> methodHandler) {
        String methodName = methodHandler.getMethodName();
        if (methodRegistry.containsKey(methodName)) {
            throw new RuntimeException("Already registered method with name \"" + methodName + "\".");
        }
        methodRegistry.put(methodName, methodHandler);
    }

    public PredefinedMethod<E, ID, R> lookupPredefinedMethod(String methodName) {
        return methodRegistry.get(methodName);
    }

    public void registerDynamicMethod(String methodName, DynamicMethod<E, ID, R> dynamicMethod) {
        if (dynamicMethodRegistry.containsKey(methodName)) {
            // Removed regex condition, because the hashmap couldn't handle methods with the same name.
            throw new RuntimeException("Already registered dynamicMethod with name \"" + methodName + "\".");
        }
        dynamicMethodRegistry.put(methodName, dynamicMethod);
    }

    public DynamicMethod<E, ID, R> lookupDynamicMethod(String methodName) {
        return dynamicMethodRegistry.get(methodName);
    }

    @SuppressWarnings("unchecked")
    public E checkEntity(Method method, Object argument) {
        E entity = (E) argument;
        if (entity == null) {
            throw new NullPointerException("Entity of type " + entityClass.getName() + " as parameter of method " +
                    method.getName() + " is null.");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public ID checkUniqueId(Method method, Object argument) {
        ID uniqueId = (ID) argument;
        if (uniqueId == null) {
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() + " as parameter of method " +
                    method.getName() + " is null.");
        }
        return uniqueId;
    }

    @SuppressWarnings("unchecked")
    public List<E> checkEntityList(Method method, Object argument) {
        List<E> entity = (List<E>) argument;
        if (entity == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() + " as parameter of method " +
                    method.getName() + " is null.");
        }
        return entity;
    }

    public ID getUniqueId(E entity) throws IllegalAccessException {
        return entityUniqueIdClass.cast(entityUniqueIdField.get(entity));
    }

    public Bson createIdFilter(ID uniqueId) {
        return Filters.eq(entityUniqueIdField.getName(), uniqueId);
    }
}
