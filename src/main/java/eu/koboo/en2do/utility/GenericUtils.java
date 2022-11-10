package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@UtilityClass
public class GenericUtils {

    public Class<?> getGenericTypeOfReturnList(Method method) {
        Type returnType = method.getGenericReturnType();
        ParameterizedType type = (ParameterizedType) returnType;
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    public Class<?> getGenericTypeOfParameterList(Method method, int paramIndex) {
        Parameter parameter = method.getParameters()[paramIndex];
        Type parameterType = parameter.getParameterizedType();
        ParameterizedType type = (ParameterizedType) parameterType;
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    public boolean isTypeOf(Class<?> class1, Class<?> class2) {
        if (isBoolean(class1) && isBoolean(class2)) {
            return true;
        }
        if (isShort(class1) && isShort(class2)) {
            return true;
        }
        if (isFloat(class1) && isFloat(class2)) {
            return true;
        }
        if (isInteger(class1) && isInteger(class2)) {
            return true;
        }
        if (isLong(class1) && isLong(class2)) {
            return true;
        }
        if (isDouble(class1) && isDouble(class2)) {
            return true;
        }
        if (isChar(class1) && isChar(class2)) {
            return true;
        }
        return class1.isAssignableFrom(class2);
    }

    private boolean isBoolean(Class<?> clazz) {
        return clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class);
    }

    private boolean isShort(Class<?> clazz) {
        return clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class);
    }

    private boolean isFloat(Class<?> clazz) {
        return clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class);
    }

    private boolean isInteger(Class<?> clazz) {
        return clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class);
    }

    private boolean isLong(Class<?> clazz) {
        return clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class);
    }

    private boolean isDouble(Class<?> clazz) {
        return clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class);
    }

    private boolean isChar(Class<?> clazz) {
        return clazz.isAssignableFrom(Character.class) || clazz.isAssignableFrom(char.class);
    }
}
