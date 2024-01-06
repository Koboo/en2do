package eu.koboo.en2do.mongodb;

import eu.koboo.en2do.operators.FilterOperator;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidListParameterException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidRegexParameterException;
import eu.koboo.en2do.mongodb.exception.methods.MethodMismatchingTypeException;
import eu.koboo.en2do.mongodb.exception.methods.MethodParameterNotFoundException;
import eu.koboo.en2do.mongodb.exception.repository.*;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.TransformField;
import eu.koboo.en2do.repository.entity.Transient;
import eu.koboo.en2do.utility.FieldUtils;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.experimental.UtilityClass;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
     * Validates the compatibility of the given type class
     *
     * @param codecRegistry   The codecRegistry to look for a codec
     * @param repositoryClass The class of the repository
     * @param typeClass       The type class, which should be validated
     * @param <E>             The generic type of the entity
     * @param <ID>            The generic type of the id of the entity
     * @param <R>             The generic type of the repository
     * @throws Exception if type class is not valid.
     */
    public <E, ID, R extends Repository<E, ID>> void validateCompatibility(
        CodecRegistry codecRegistry, Class<R> repositoryClass, Class<?> typeClass) throws Exception {
        if (typeClass == null) {
            throw new RuntimeException("Class for validation is null! Please open an issue on github!");
        }
        // It's a primitive? No validation needed for that.
        if (typeClass.isPrimitive()) {
            return;
        }
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
        Constructor<?>[] entityConstructors = typeClass.getConstructors();
        for (Constructor<?> constructor : entityConstructors) {
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
            throw new RepositoryConstructorException(typeClass, repositoryClass);
        }

        // No fields found? That's too bad. We need something to save.
        Set<Field> fieldSet = FieldUtils.collectFields(typeClass);
        if (fieldSet.isEmpty()) {
            throw new RepositoryNoFieldsException(typeClass, repositoryClass);
        }

        try {
            // Getting beanInfo of the type class
            BeanInfo beanInfo = Introspector.getBeanInfo(typeClass);
            Set<String> fieldNameSet = new HashSet<>();
            Set<String> bsonNameSet = new HashSet<>();

            // PropertyDescriptors represent all fields of the entity, even the extended fields.
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

                // Ignore "class" descriptor.
                if (descriptor.getName().equalsIgnoreCase("class")) {
                    continue;
                }

                // Search for the field by the descriptor name.
                Field field = fieldSet.stream()
                    .filter(f -> f.getName().equals(descriptor.getName()))
                    .findFirst()
                    .orElse(null);
                if (field == null) {
                    continue;
                }

                // Ignore all fields annotated with transient,
                // because we don't want to save that anyway.
                if (field.isAnnotationPresent(Transient.class)) {
                    continue;
                }

                // Final fields are strictly prohibited.
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new RepositoryFinalFieldException(field, repositoryClass);
                }

                // Check the declaration of the setter method.
                // It needs to be public and have exactly 1 parameter.
                Method writeMethod = descriptor.getWriteMethod();
                if (writeMethod == null) {
                    throw new RepositorySetterNotFoundException(typeClass, repositoryClass, field.getName());
                }
                if (writeMethod.getParameterCount() != 1) {
                    throw new RepositoryInvalidSetterException(typeClass, repositoryClass, field.getName());
                }
                if (!Modifier.isPublic(writeMethod.getModifiers())) {
                    throw new RepositoryInvalidSetterException(typeClass, repositoryClass, field.getName());
                }

                // Check the declaration of the getter method.
                // It needs to be public and have exactly 0 parameters.
                Method readMethod = descriptor.getReadMethod();
                if (readMethod == null) {
                    throw new RepositoryGetterNotFoundException(typeClass, repositoryClass, field.getName());
                }
                if (readMethod.getParameterCount() != 0) {
                    throw new RepositoryInvalidGetterException(typeClass, repositoryClass, field.getName());
                }
                if (!Modifier.isPublic(readMethod.getModifiers())) {
                    throw new RepositoryInvalidGetterException(typeClass, repositoryClass, field.getName());
                }

                // Validate the typeClass recursively.
                validateCompatibility(codecRegistry, repositoryClass, field.getType());

                // Check for duplicated field names.
                String lowerCaseName = field.getName().toLowerCase(Locale.ROOT);
                if (fieldNameSet.contains(lowerCaseName)) {
                    throw new RepositoryDuplicatedFieldException(field, repositoryClass);
                }
                fieldNameSet.add(lowerCaseName);

                // Check if the field has a valid TransformField annotation
                TransformField transformField = field.getAnnotation(TransformField.class);
                if (transformField != null && transformField.value().trim().equalsIgnoreCase("")) {
                    throw new RepositoryInvalidFieldNameException(typeClass, repositoryClass, field.getName());
                }

                // Define the bson document field name by checking for the transform field annotation.
                String bsonName;
                if (transformField != null) {
                    bsonName = transformField.value();
                } else {
                    bsonName = field.getName();
                }

                // Check for duplicated field names, again.
                if (bsonNameSet.contains(bsonName.toLowerCase(Locale.ROOT))) {
                    throw new RepositoryDuplicatedFieldException(field, repositoryClass);
                }
                bsonNameSet.add(bsonName.toLowerCase(Locale.ROOT));
            }
            fieldNameSet.clear();
            fieldSet.clear();
            bsonNameSet.clear();
        } catch (IntrospectionException e) {
            throw new RepositoryBeanInfoNotFoundException(typeClass, repositoryClass, e);
        }
    }

    public void validateTypes(Class<?> repositoryClass, Method method, Field field, FilterOperator filterOperator,
                              int currentParameterIndex) throws Exception {
        int operatorParameterCount = filterOperator.getExpectedParameterCount();
        Class<?> fieldClass = field.getType();
        for (int i = 0; i < filterOperator.getExpectedParameterCount(); i++) {
            int paramIndex = currentParameterIndex + i;
            Class<?> paramClass = method.getParameters()[paramIndex].getType();
            if (paramClass == null) {
                throw new MethodParameterNotFoundException(method, repositoryClass,
                    (paramIndex + operatorParameterCount),
                    method.getParameterCount());
            }

            // Special checks for some operators
            switch (filterOperator) {
                case REGEX:
                    // Regex filter allows two types as parameters.
                    if (GenericUtils.isNotTypeOf(String.class, paramClass) && GenericUtils.isNotTypeOf(Pattern.class, paramClass)) {
                        throw new MethodInvalidRegexParameterException(method, repositoryClass, paramClass);
                    }
                    break;
                case IN:
                    if (paramClass.isArray()) {
                        Class<?> arrayType = paramClass.getComponentType();
                        if (GenericUtils.isNotTypeOf(fieldClass, arrayType)) {
                            throw new MethodInvalidListParameterException(method, repositoryClass, fieldClass, arrayType);
                        }
                        break;
                    }
                    // In filter only allows list. Maybe arrays in future releases.
                    if (!GenericUtils.isNotTypeOf(java.util.Collection.class, paramClass)) {
                        Class<?> listType = GenericUtils.getGenericTypeOfParameter(method, paramIndex);
                        if (GenericUtils.isNotTypeOf(fieldClass, listType)) {
                            throw new MethodInvalidListParameterException(method, repositoryClass, fieldClass, listType);
                        }
                        break;
                    }
                    throw new MethodMismatchingTypeException(method, repositoryClass, java.util.Collection.class, paramClass);
                case HAS_KEY:
                    if (GenericUtils.isNotTypeOf(Map.class, fieldClass)) {
                        throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                    }
                    Class<?> keyClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (GenericUtils.isNotTypeOf(keyClass, paramClass)) {
                        throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                    }
                    break;
                case HAS:
                    if (!GenericUtils.isNotTypeOf(Collection.class, fieldClass)) {
                        Class<?> listType = GenericUtils.getGenericTypeOfField(field, 0);
                        if (GenericUtils.isNotTypeOf(paramClass, listType)) {
                            throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                        }
                    }
                    break;
                default:
                    if (GenericUtils.isNotTypeOf(fieldClass, paramClass)) {
                        throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                    }
                    break;
            }
        }
    }
}
