package eu.koboo.en2do.mongodb.mapping;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

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

    /**
     * Resolves Java names used by index annotations, which allow case-insensitive names.
     */
    public FieldMapping findByJavaNameIgnoreCase(String name) {
        FieldMapping exactMatch = findByJavaName(name);
        if (exactMatch != null) {
            return exactMatch;
        }
        for (FieldMapping field : fieldsByJavaName.values()) {
            if (field.getJavaName().equalsIgnoreCase(name)) {
                return field;
            }
        }
        return null;
    }

    public FieldMapping findByBsonName(String name) {
        return fieldsByBsonName.get(name);
    }
}
