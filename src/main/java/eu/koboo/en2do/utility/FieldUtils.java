package eu.koboo.en2do.utility;

import eu.koboo.en2do.repository.entity.TransformField;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for everything related to fields.
 */
@UtilityClass
@SuppressWarnings("unused")
public class FieldUtils {

    /**
     * This method is used to scan a class for all fields.
     *
     * @param typeClass The class, which should be scanned
     * @param <E>       The generic type of the class
     * @return The Set with all found fields of the given class.
     */
    public <E> Set<Field> collectFields(Class<E> typeClass) {
        Set<Field> fields = new HashSet<>();
        Class<?> clazz = typeClass;
        while (clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            clazz = clazz.getSuperclass();
            if (declaredFields.length == 0) {
                continue;
            }
            fields.addAll(Arrays.asList(declaredFields));
        }
        return fields;
    }

    /**
     * This method is used to iterate through a set of fields and search for a field by its name.
     *
     * @param fieldName The field name, which should be searched.
     * @param fieldSet  The Set, which should be iterated through
     * @return The field, if found. If not found, it returns "null"
     */
    public Field findFieldByName(String fieldName, Set<Field> fieldSet) {
        for (Field field : fieldSet) {
            String entityFieldName = field.getName();
            if (!entityFieldName.equals(fieldName)) {
                continue;
            }
            return field;
        }
        return null;
    }

    public String parseBsonName(Field field) {
        TransformField transformField = field.getAnnotation(TransformField.class);
        if (transformField != null && !transformField.value().trim().equalsIgnoreCase("")) {
            return transformField.value();
        }
        return field.getName();
    }
}
