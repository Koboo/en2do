package eu.koboo.en2do.internal;

import com.mongodb.MongoClientSettings;
import eu.koboo.en2do.internal.exception.repository.*;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Transient;
import eu.koboo.en2do.utility.FieldUtils;
import org.bson.codecs.Codec;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Validator {

    private static Codec<?> getCodec(Class<?> typeClass) {
        try {
            return MongoClientSettings.getDefaultCodecRegistry().get(typeClass);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static <E, ID, R extends Repository<E, ID>> void validateCompatibility(
            Class<R> repositoryClass, Class<?> typeClass) throws Exception {
        if(typeClass == null) {
            throw new RuntimeException("Class for validation is null! Please open an issue on github!");
        }
        // It's a primitive? No validation needed for that.
        if(typeClass.isPrimitive()) {
            return;
        }
        // We already got a codec for the type? No validation needed for that.
        Codec<?> typeCodec = getCodec(typeClass);
        if(typeCodec != null) {
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
        }
        if(!hasValidConstructor) {
            throw new RepositoryConstructorException(typeClass, repositoryClass);
        }

        // No fields found? That's too bad. We need something to save.
        Set<Field> fieldSet = FieldUtils.collectFields(typeClass);
        if(fieldSet.isEmpty()) {
            throw new RepositoryNoFieldsException(typeClass, repositoryClass);
        }

        try {
            //TODO: Recursively getting propertyDescriptors.
            BeanInfo beanInfo = Introspector.getBeanInfo(typeClass);
            Set<String> fieldNameSet = new HashSet<>();
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                // Ignore "class" descriptor.
                if(descriptor.getName().equalsIgnoreCase("class")) {
                    continue;
                }

                // Search for the field by the descriptor name.
                Field field = fieldSet.stream()
                        .filter(f -> f.getName().equals(descriptor.getName()))
                        .findFirst()
                        .orElse(null);
                if(field == null) {
                    throw new RepositoryDescriptorException(typeClass, repositoryClass, descriptor.getName());
                }

                // Ignore all fields annotated with transient, because pojo doesn't touch that.
                if(field.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                // Final fields are strictly not allowed.
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new RepositoryFinalFieldException(field, repositoryClass);
                }

                // Check the declaration of the setter method. It needs to be public and have exactly 1 parameter.
                Method writeMethod = descriptor.getWriteMethod();
                if(writeMethod == null) {
                    throw new RepositorySetterNotFoundException(typeClass, repositoryClass, field.getName());
                }
                if(writeMethod.getParameterCount() != 1) {
                    throw new RepositoryInvalidSetterException(typeClass, repositoryClass, field.getName());
                }
                if(!Modifier.isPublic(writeMethod.getModifiers())) {
                    throw new RepositoryInvalidSetterException(typeClass, repositoryClass, field.getName());
                }

                // Check the declaration of the getter method. It needs to be public and have exactly 0 parameter.
                Method readMethod = descriptor.getReadMethod();
                if(readMethod == null) {
                    throw new RepositoryGetterNotFoundException(typeClass, repositoryClass, field.getName());
                }
                if(readMethod.getParameterCount() != 0) {
                    throw new RepositoryInvalidGetterException(typeClass, repositoryClass, field.getName());
                }
                if(!Modifier.isPublic(readMethod.getModifiers())) {
                    throw new RepositoryInvalidGetterException(typeClass, repositoryClass, field.getName());
                }

                // Validate the typeClass recursively.
                validateCompatibility(repositoryClass, field.getType());

                // Check for duplicated field names.
                String lowerCaseName = field.getName().toLowerCase(Locale.ROOT);
                if(fieldNameSet.contains(lowerCaseName)) {
                    throw new RepositoryDuplicatedFieldException(field, repositoryClass);
                }
                fieldNameSet.add(lowerCaseName);

                //TODO: Validate annotations
            }
            fieldNameSet.clear();
            fieldSet.clear();
        } catch (IntrospectionException e) {
            throw new RepositoryBeanInfoNotFoundException(typeClass, repositoryClass, e);
        }
    }
}
