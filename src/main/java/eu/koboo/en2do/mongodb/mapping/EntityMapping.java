package eu.koboo.en2do.mongodb.mapping;

import eu.koboo.en2do.utility.reflection.PrimitiveUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Describes the persisted fields of an entity and their BSON names.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class EntityMapping<E> {

    Class<E> entityClass;

    /**
     * The mapped ID field, or {@code null} for types without an {@code @Id} field.
     */
    FieldMapping idField;

    Map<String, FieldMapping> fieldsByJavaName;
    Map<String, FieldMapping> fieldsByBsonName;

    EntityMapping(Class<E> entityClass, FieldMapping idField, List<FieldMapping> fields) {
        this.entityClass = entityClass;
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
    public <ID> Class<ID> getIdType() {
        Class<?> rawIdType = idField.getType();
        Class<?> mappedIdType = PrimitiveUtils.toWrapper(rawIdType);
        return (Class<ID>) mappedIdType;
    }

    public E asEntity(Object object) {
        if (object == null) {
            throw new NullPointerException("Entity of type " + entityClass.getName() + " is null.");
        }
        return entityClass.cast(object);
    }

    @SuppressWarnings("unchecked")
    public Collection<E> asEntityCollection(Object argument) {
        if (argument == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() + " is null.");
        }
        return (Collection<E>) argument;
    }

    public <ID> ID asEntityId(Object object) {
        if (object == null) {
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() + " is null.");
        }
        Class<ID> idType = getIdType();
        return idType.cast(object);
    }

    public <ID> ID getIdFromEntity(Object object) throws Exception {
        E entity = asEntity(object);
        Field field = idField.getField();
        Class<ID> idType = getIdType();
        ID uniqueId = idType.cast(field.get(entity));
        if (uniqueId == null) {
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() + " is null.");
        }
        return uniqueId;
    }
}
