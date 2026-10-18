package eu.koboo.en2do.factory.mapping;

import eu.koboo.en2do.mongodb.exception.RepositoryException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIdMismatchException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIdNotFoundException;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.TransformField;
import eu.koboo.en2do.repository.entity.Transient;
import eu.koboo.en2do.utility.reflection.PrimitiveUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.koboo.en2do.factory.mapping.MappingUtils.invalidField;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RepositoryMapping {

    Class<?> repositoryClass;
    Class<?> entityClass;
    FieldMapping idField;

    Map<String, FieldMapping> fieldsByJavaName;
    Map<String, FieldMapping> fieldsByBsonName;

    RepositoryMapping(Class<?> repositoryClass) {
        if (!Repository.class.isAssignableFrom(repositoryClass)) {
            throw new RepositoryException("repositoryClass is not of type Repository.", repositoryClass);
        }

        ParameterizedType repositoryType = MappingUtils.resolveRepositoryType(repositoryClass);
        Type[] typeArguments = repositoryType.getActualTypeArguments();
        Class<?> repositoryEntityClass = (Class<?>) typeArguments[0];
        Class<?> repositoryIdClass = (Class<?>) typeArguments[1];

        List<FieldMapping> fields = new ArrayList<>();
        Set<String> javaNames = new HashSet<>();
        Set<String> bsonNames = new HashSet<>();
        FieldMapping idField = null;

        Class<?> currentClass = repositoryEntityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (field.isSynthetic()
                    || Modifier.isStatic(modifiers)
                    || Modifier.isTransient(modifiers)
                    || field.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                if (Modifier.isFinal(modifiers)) {
                    throw invalidField(repositoryEntityClass, field, "Persisted fields cannot be final.");
                }
                String javaName = field.getName();
                if (!javaNames.add(javaName.toLowerCase(Locale.ROOT))) {
                    throw invalidField(repositoryEntityClass, field, "Duplicate Java field name: " + javaName);
                }

                String bsonName = javaName;
                TransformField transform = field.getAnnotation(TransformField.class);
                if (transform != null) {
                    if (transform.value().trim().isEmpty()) {
                        throw invalidField(repositoryEntityClass, field, "@TransformField cannot specify a blank name.");
                    }
                    bsonName = transform.value();
                }

                boolean isId = field.isAnnotationPresent(Id.class);
                if (isId) {
                    if (idField != null) {
                        throw invalidField(repositoryEntityClass, field, "Multiple @Id fields: " + idField.getJavaName() + " and " + javaName);
                    }
                    bsonName = "_id";
                }
                if (!bsonNames.add(bsonName.toLowerCase(Locale.ROOT))) {
                    throw invalidField(repositoryEntityClass, field, "Duplicate BSON field name: " + bsonName);
                }

                Class<?> fieldType = field.getType();
                FieldMapping mapping = new FieldMapping(field, fieldType, javaName, bsonName);
                if (isId) {
                    if (!field.trySetAccessible()) {
                        throw invalidField(repositoryEntityClass, field, "The @Id field is not accessible.");
                    }
                    idField = mapping;
                }
                fields.add(mapping);
            }
            currentClass = currentClass.getSuperclass();
        }
        if (idField == null) {
            throw new RepositoryTypeIdNotFoundException(repositoryClass, repositoryEntityClass);
        }
        Class<?> mappingIdClass = PrimitiveUtils.toWrapper(idField.getType());
        if (!repositoryIdClass.equals(mappingIdClass)) {
            throw new RepositoryTypeIdMismatchException(repositoryClass, repositoryEntityClass, repositoryIdClass, mappingIdClass);
        }

        this.repositoryClass = repositoryClass;
        this.entityClass = repositoryEntityClass;
        this.idField = idField;
        this.fieldsByJavaName = fields.stream()
            .collect(Collectors.toUnmodifiableMap(
                FieldMapping::getJavaName,
                Function.identity()
            ));
        this.fieldsByBsonName = fields.stream()
            .collect(Collectors.toUnmodifiableMap(
                FieldMapping::getBsonName,
                Function.identity()
            ));
    }


    public FieldMapping findByJavaName(String name) {
        return fieldsByJavaName.get(name);
    }

    public FieldMapping findByBsonName(String name) {
        return fieldsByBsonName.get(name);
    }

    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> Class<R> typedRepositoryClass() {
        return (Class<R>) repositoryClass;
    }

    @SuppressWarnings("unchecked")
    public <E> Class<E> typedEntityClass() {
        return (Class<E>) entityClass;
    }

    @SuppressWarnings("unchecked")
    public <ID> Class<ID> typedIdClass() {
        Class<?> rawIdType = idField.getType();
        Class<?> mappedIdType = PrimitiveUtils.toWrapper(rawIdType);
        return (Class<ID>) mappedIdType;
    }

    @SuppressWarnings("unchecked")
    public <E> E asEntity(Object object) {
        if (object == null) {
            throw new NullPointerException("Entity of type " + entityClass.getName() + " is null.");
        }
        return (E) entityClass.cast(object);
    }

    @SuppressWarnings("unchecked")
    public <E> Collection<E> asEntityCollection(Object argument) {
        if (argument == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() + " is null.");
        }
        return (Collection<E>) argument;
    }

    public <ID> ID asEntityId(Object object) {
        if (object == null) {
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() + " is null.");
        }
        Class<ID> idType = typedIdClass();
        return idType.cast(object);
    }

    public <E, ID> ID getIdFromEntity(Object object) throws Exception {
        E entity = asEntity(object);
        Field field = idField.getField();
        Class<ID> idType = typedIdClass();
        ID uniqueId = idType.cast(field.get(entity));
        if (uniqueId == null) {
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() + " is null.");
        }
        return uniqueId;
    }
}
