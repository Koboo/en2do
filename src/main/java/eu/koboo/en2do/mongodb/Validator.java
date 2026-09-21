package eu.koboo.en2do.mongodb;

import com.mongodb.client.model.geojson.Geometry;
import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidListParameterException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidRegexParameterException;
import eu.koboo.en2do.mongodb.exception.methods.MethodMismatchingTypeException;
import eu.koboo.en2do.mongodb.exception.methods.MethodParameterNotFoundException;
import eu.koboo.en2do.mongodb.exception.repository.*;
import eu.koboo.en2do.mongodb.mapping.EntityMapping;
import eu.koboo.en2do.mongodb.mapping.EntityMappingFactory;
import eu.koboo.en2do.mongodb.mapping.FieldMapping;
import eu.koboo.en2do.operators.FilterOperator;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.geo.Geo;
import eu.koboo.en2do.utility.reflection.PrimitiveUtils;
import lombok.experimental.UtilityClass;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents the validation of a class. This can be an entity or an embedded class inside the entity.
 */
@UtilityClass
public class Validator {

    /**
     * Returns a codec for the given type class or if no codec is found, it returns null.
     *
     * @param typeClass The type class to search a codec for.
     * @return The codec if found, otherwise null.
     */
    private Codec<?> getCodec(CodecRegistry codecRegistry, Class<?> typeClass) {
        try {
            return codecRegistry.get(typeClass);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Validates codec and accessor compatibility using the resolved entity mapping.
     *
     * @param codecRegistry   The codecRegistry to look for a codec
     * @param repositoryClass The class of the repository
     * @param entityMapping   The resolved mapping to validate
     * @param <E>             The generic type of the entity
     * @param <ID>            The generic type of the id of the entity
     * @param <R>             The generic type of the repository
     */
    public <E, ID, R extends Repository<E, ID>> void validateCompatibility(
        CodecRegistry codecRegistry,
        Class<R> repositoryClass,
        EntityMapping<?> entityMapping) {
        Objects.requireNonNull(entityMapping, "entityMapping");
        Class<?> typeClass = entityMapping.getEntityClass();
        // Probably a generic type. I'm NOT validating every "Object" type for you.
        // Keep track of your own code.
        if (typeClass.equals(Object.class)) {
            return;
        }
        // We already got a codec for the type? No validation needed for that.
        Codec<?> typeCodec = getCodec(codecRegistry, typeClass);
        if (typeCodec != null) {
            return;
        }

        // Validating the no-args public constructor for the given type class
        boolean hasValidConstructor = false;
        for (Constructor<?> constructor : typeClass.getConstructors()) {
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }
            if (constructor.getParameterCount() > 0) {
                continue;
            }
            hasValidConstructor = true;
            break;
        }
        if (!hasValidConstructor) {
            throw new RepositoryTypeConstructorException(typeClass, repositoryClass);
        }

        // No fields found? That's too bad. We need something to save.
        if (entityMapping.getFieldsByJavaName().isEmpty()) {
            throw new RepositoryTypeFieldInvalidException(typeClass, repositoryClass);
        }

        try {
            // Getting beanInfo of the type class
            BeanInfo beanInfo = Introspector.getBeanInfo(typeClass);

            // PropertyDescriptors represent all fields of the entity, even the extended fields.
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

                // Ignore "class" descriptor.
                if (descriptor.getName().equalsIgnoreCase("class")) {
                    continue;
                }

                FieldMapping field = entityMapping.findByJavaName(descriptor.getName());
                if (field == null) {
                    continue;
                }

                // Check the declaration of the setter method.
                // It needs to be public and have exactly 1 parameter.
                Method writeMethod = descriptor.getWriteMethod();
                if (writeMethod == null) {
                    throw new RepositorySetterNotFoundException(typeClass, repositoryClass, field.getJavaName());
                }
                if (writeMethod.getParameterCount() != 1) {
                    throw new RepositorySetterInvalidException(typeClass, repositoryClass, field.getJavaName());
                }
                if (!Modifier.isPublic(writeMethod.getModifiers())) {
                    throw new RepositorySetterInvalidException(typeClass, repositoryClass, field.getJavaName());
                }

                // Check the declaration of the getter method.
                // It needs to be public and have exactly 0 parameters.
                Method readMethod = descriptor.getReadMethod();
                if (readMethod == null) {
                    throw new RepositoryGetterNotFoundException(typeClass, repositoryClass, field.getJavaName());
                }
                if (readMethod.getParameterCount() != 0) {
                    throw new RepositoryGetterInvalidException(typeClass, repositoryClass, field.getJavaName());
                }
                if (!Modifier.isPublic(readMethod.getModifiers())) {
                    throw new RepositoryGetterInvalidException(typeClass, repositoryClass, field.getJavaName());
                }

                // Only embedded types without a codec need their own field mapping.
                Class<?> fieldType = field.getType();
                if (fieldType == Object.class || getCodec(codecRegistry, fieldType) != null) {
                    continue;
                }
                EntityMapping<?> embeddedMapping;
                try {
                    embeddedMapping = EntityMappingFactory.create(fieldType);
                } catch (IllegalArgumentException e) {
                    throw new RepositoryTypeException("Invalid entity mapping.", repositoryClass, fieldType, e);
                }
                validateCompatibility(codecRegistry, repositoryClass, embeddedMapping);
            }
        } catch (IntrospectionException e) {
            throw new RepositoryTypeBeanInfoException(typeClass, repositoryClass, e);
        }
    }

    public void validateParameterTypes(Class<?> repositoryClass,
                                       Method method,
                                       FieldMapping field,
                                       FilterOperator filterOperator,
                                       int currentParameterIndex) {
        int operatorParameterCount = filterOperator.getExpectedParameterCount();
        Class<?> fieldClass = field.getType();
        fieldClass = PrimitiveUtils.wrapperOf(fieldClass);
        for (int i = 0; i < filterOperator.getExpectedParameterCount(); i++) {
            int paramIndex = currentParameterIndex + i;
            Class<?> paramClass = method.getParameters()[paramIndex].getType();
            if (paramClass == null) {
                throw new MethodParameterNotFoundException(repositoryClass, method,
                    (paramIndex + operatorParameterCount),
                    method.getParameterCount());
            }
            paramClass = PrimitiveUtils.wrapperOf(paramClass);

            // Special checks for some operators
            switch (filterOperator) {
                case REGEX:
                    // Regex filter allows two types as parameters.
                    if (!String.class.isAssignableFrom(paramClass) && !Pattern.class.isAssignableFrom(paramClass)) {
                        throw new MethodInvalidRegexParameterException(repositoryClass, method, paramClass);
                    }
                    break;
                case IN:
                    if (paramClass.isArray()) {
                        Class<?> arrayType = paramClass.getComponentType();
                        if (!arrayType.isAssignableFrom(fieldClass)) {
                            throw new MethodInvalidListParameterException(repositoryClass, method, fieldClass, arrayType);
                        }
                        break;
                    }
                    // In filter only allows list. Maybe arrays in future releases.
                    if (Collection.class.isAssignableFrom(paramClass)) {
                        Parameter parameter = method.getParameters()[paramIndex];
                        Type parameterType = parameter.getParameterizedType();
                        ParameterizedType type = (ParameterizedType) parameterType;
                        Class<?> listType = (Class<?>) type.getActualTypeArguments()[0];
                        if (!listType.isAssignableFrom(fieldClass)) {
                            throw new MethodInvalidListParameterException(repositoryClass, method, fieldClass, listType);
                        }
                        break;
                    }
                    throw new MethodMismatchingTypeException(repositoryClass, method, Collection.class, paramClass);
                case HAS_KEY:
                    if (!Map.class.isAssignableFrom(fieldClass)) {
                        throw new MethodMismatchingTypeException(repositoryClass, method, fieldClass, paramClass);
                    }
                    ParameterizedType hasKeyParameterizedType = (ParameterizedType) field.getGenericType();
                    Class<?> keyClass = (Class<?>) hasKeyParameterizedType.getActualTypeArguments()[0];
                    if (!keyClass.isAssignableFrom(paramClass)) {
                        throw new MethodMismatchingTypeException(repositoryClass, method, fieldClass, paramClass);
                    }
                    break;
                case HAS:
                    if (Collection.class.isAssignableFrom(fieldClass)) {
                        ParameterizedType hasParameterizedType = (ParameterizedType) field.getGenericType();
                        Class<?> listType = (Class<?>) hasParameterizedType.getActualTypeArguments()[0];
                        if (!listType.isAssignableFrom(paramClass)) {
                            throw new MethodMismatchingTypeException(repositoryClass, method, fieldClass, paramClass);
                        }
                    }
                    break;
                case GEO:
                    if (!Geometry.class.isAssignableFrom(fieldClass)) {
                        throw new RuntimeException("Field is not of type geometry!");
                    }
                    if (!Geo.class.isAssignableFrom(paramClass)) {
                        throw new MethodMismatchingTypeException(repositoryClass, method, Geo.class, paramClass);
                    }
                    break;
                default:
                    if (!paramClass.isAssignableFrom(fieldClass)) {
                        throw new MethodMismatchingTypeException(repositoryClass, method, fieldClass, paramClass);
                    }
                    break;
            }
        }
    }
}
