package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A utility class for everything related to generic types or class types.
 */
@UtilityClass
public class GenericUtils {

    /**
     * Gets the generic type of the return type from the given method
     * @param method The method with the return type
     * @return The class of the generic type
     */
    public Class<?> getGenericTypeOfReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        ParameterizedType type = (ParameterizedType) returnType;
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    /**
     * Gets the generic type of the parameter at the given from the given method
     * @param method The method with the parameter
     * @param paramIndex The index of the parameter
     * @return The class of the generic type of the parameter
     */
    public Class<?> getGenericTypeOfParameter(Method method, int paramIndex) {
        Parameter parameter = method.getParameters()[paramIndex];
        Type parameterType = parameter.getParameterizedType();
        ParameterizedType type = (ParameterizedType) parameterType;
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    /**
     * Checks if class1 and class2 is not the same type
     * @param class1 The first given class
     * @param class2 The second given class
     * @return true if class1 and class2 are not same type type
     */
    public boolean isNotTypeOf(Class<?> class1, Class<?> class2) {
        if (isBoolean(class1) && isBoolean(class2)) {
            return false;
        }
        if (isShort(class1) && isShort(class2)) {
            return false;
        }
        if (isFloat(class1) && isFloat(class2)) {
            return false;
        }
        if (isInteger(class1) && isInteger(class2)) {
            return false;
        }
        if (isLong(class1) && isLong(class2)) {
            return false;
        }
        if (isDouble(class1) && isDouble(class2)) {
            return false;
        }
        if (isChar(class1) && isChar(class2)) {
            return false;
        }
        return !class1.isAssignableFrom(class2);
    }

    /**
     * Checks if the given class is a type of "boolean"
     * @param clazz The class to check
     * @return true, if the class is type boolean
     */
    private boolean isBoolean(Class<?> clazz) {
        return clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class);
    }

    /**
     * Checks if the given class is a type of "short"
     * @param clazz The class to check
     * @return true, if the class is type short
     */
    private boolean isShort(Class<?> clazz) {
        return clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class);
    }

    /**
     * Checks if the given class is a type of "float"
     * @param clazz The class to check
     * @return true, if the class is type float
     */
    private boolean isFloat(Class<?> clazz) {
        return clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class);
    }

    /**
     * Checks if the given class is a type of "int"
     * @param clazz The class to check
     * @return true, if the class is type int
     */
    private boolean isInteger(Class<?> clazz) {
        return clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class);
    }

    /**
     * Checks if the given class is a type of "long"
     * @param clazz The class to check
     * @return true, if the class is type long
     */
    private boolean isLong(Class<?> clazz) {
        return clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class);
    }

    /**
     * Checks if the given class is a type of "double"
     * @param clazz The class to check
     * @return true, if the class is type double
     */
    private boolean isDouble(Class<?> clazz) {
        return clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class);
    }

    /**
     * Checks if the given class is a type of "char"
     * @param clazz The class to check
     * @return true, if the class is type char
     */
    private boolean isChar(Class<?> clazz) {
        return clazz.isAssignableFrom(Character.class) || clazz.isAssignableFrom(char.class);
    }
}
