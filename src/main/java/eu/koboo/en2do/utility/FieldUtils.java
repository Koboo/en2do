package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class FieldUtils {

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

    public Field findFieldByName(String fieldName, Set<Field> fieldSet) {
        for (Field field : fieldSet) {
            if (!field.getName().equalsIgnoreCase(fieldName)) {
                continue;
            }
            return field;
        }
        return null;
    }
}
