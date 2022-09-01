package eu.koboo.en2do.utility;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericUtils {

    public static Class<?> getGenericTypeOfReturnedList(Method method) {
        Type returnType = method.getGenericReturnType();
        ParameterizedType type = (ParameterizedType) returnType;
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    public static boolean isTypeOf(Class<?> class1, Class<?> class2) {
        if(isBoolean(class1) && isBoolean(class2)) {
            return true;
        }
        if(isShort(class1) && isShort(class2)) {
            return true;
        }
        if(isFloat(class1) && isFloat(class2)) {
            return true;
        }
        if(isInteger(class1) && isInteger(class2)) {
            return true;
        }
        if(isLong(class1) && isLong(class2)) {
            return true;
        }
        if(isDouble(class1) && isDouble(class2)) {
            return true;
        }
        if(isChar(class1) && isChar(class2)) {
            return true;
        }
        return class1.isAssignableFrom(class2);
    }

    private static boolean isBoolean(Class<?> clazz) {
        return clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class);
    }

    private static boolean isShort(Class<?> clazz) {
        return clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class);
    }

    private static boolean isFloat(Class<?> clazz) {
        return clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class);
    }

    private static boolean isInteger(Class<?> clazz) {
        return clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class);
    }

    private static boolean isLong(Class<?> clazz) {
        return clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class);
    }

    private static boolean isDouble(Class<?> clazz) {
        return clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class);
    }

    private static boolean isChar(Class<?> clazz) {
        return clazz.isAssignableFrom(Character.class) || clazz.isAssignableFrom(char.class);
    }
}