package eu.koboo.en2do.mongodb.mapping;

import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.TransformField;
import eu.koboo.en2do.repository.entity.Transient;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Resolves field mappings without requiring a repository or MongoDB connection.
 */
@UtilityClass
public class EntityMappingFactory {

    private static final ClassValue<EntityMapping<?>> ENTITY_MAPPING_CACHE = new ClassValue<>() {
        @Override
        protected EntityMapping<?> computeValue(Class<?> entityClass) {
            return createMapping(entityClass);
        }
    };

    /**
     * Creates a mapping from an entity's fields, including inherited fields.
     * Static, synthetic, and transient fields are excluded. Persisted final fields,
     * blank transformed names, duplicate names, and multiple IDs are rejected.
     * Name collisions are checked without regard to case, matching repository validation.
     *
     * <p>An ID is optional for embedded objects. Repository validation must enforce
     * its presence and type when the mapping is used for a repository entity.
     * Method properties and codec compatibility are handled separately.
     *
     * @param entityClass the entity class to inspect
     * @param <E> the entity type
     * @return the resolved field mapping
     * @throws IllegalArgumentException if the field mapping is invalid
     */
    public <E> EntityMapping<E> create(Class<E> entityClass) {
        Objects.requireNonNull(entityClass, "entityClass");

        @SuppressWarnings("unchecked")
        EntityMapping<E> entityMapping = (EntityMapping<E>) ENTITY_MAPPING_CACHE.get(entityClass);
        return entityMapping;
    }

    private EntityMapping<?> createMapping(Class<?> entityClass) {

        List<FieldMapping> fields = new ArrayList<>();
        Set<String> javaNames = new HashSet<>();
        Set<String> bsonNames = new HashSet<>();
        FieldMapping idField = null;

        Class<?> currentClass = entityClass;
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
                    throw invalidField(entityClass, field, "Persisted fields cannot be final.");
                }
                if (!javaNames.add(field.getName().toLowerCase(Locale.ROOT))) {
                    throw invalidField(entityClass, field, "Duplicate Java field name: " + field.getName());
                }

                String bsonName = field.getName();
                TransformField transform = field.getAnnotation(TransformField.class);
                if (transform != null) {
                    if (transform.value().trim().isEmpty()) {
                        throw invalidField(entityClass, field, "@TransformField cannot specify a blank name.");
                    }
                    bsonName = transform.value();
                }

                boolean isId = field.isAnnotationPresent(Id.class);
                if (isId) {
                    if (idField != null) {
                        throw invalidField(entityClass, field,
                            "Multiple @Id fields: " + idField.getJavaName() + " and " + field.getName());
                    }
                    bsonName = "_id";
                }
                if (!bsonNames.add(bsonName.toLowerCase(Locale.ROOT))) {
                    throw invalidField(entityClass, field, "Duplicate BSON field name: " + bsonName);
                }

                FieldMapping mapping = new FieldMapping(field, bsonName);
                if (isId) {
                    if (!field.trySetAccessible()) {
                        throw invalidField(entityClass, field, "The @Id field is not accessible.");
                    }
                    idField = mapping;
                }
                fields.add(mapping);
            }
            currentClass = currentClass.getSuperclass();
        }

        return new EntityMapping<>(entityClass, idField, fields);
    }

    private IllegalArgumentException invalidField(Class<?> entityClass, Field field, String message) {
        return new IllegalArgumentException(message + "\n"
            + "  - Entity: " + entityClass.getName() + "\n"
            + "  - Field: " + field.getDeclaringClass().getSimpleName() + "." + field.getName());
    }
}
