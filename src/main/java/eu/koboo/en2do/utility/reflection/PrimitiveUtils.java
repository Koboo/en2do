package eu.koboo.en2do.utility.reflection;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PrimitiveUtils {

    public Class<?> wrapperOf(Class<?> primitiveClass) {
        if (primitiveClass.equals(int.class)) {
            return Integer.class;
        }
        if (primitiveClass.equals(int[].class)) {
            return Integer[].class;
        }
        if (primitiveClass.equals(long.class)) {
            return Long.class;
        }
        if (primitiveClass.equals(long[].class)) {
            return Long[].class;
        }
        if (primitiveClass.equals(boolean.class)) {
            return Boolean.class;
        }
        if (primitiveClass.equals(boolean[].class)) {
            return Boolean[].class;
        }
        if (primitiveClass.equals(byte.class)) {
            return Byte.class;
        }
        if (primitiveClass.equals(byte[].class)) {
            return Byte[].class;
        }
        if (primitiveClass.equals(char.class)) {
            return Character.class;
        }
        if (primitiveClass.equals(char[].class)) {
            return Character[].class;
        }
        if (primitiveClass.equals(float.class)) {
            return Float.class;
        }
        if (primitiveClass.equals(float[].class)) {
            return Float[].class;
        }
        if (primitiveClass.equals(double.class)) {
            return Double.class;
        }
        if (primitiveClass.equals(double[].class)) {
            return Double[].class;
        }
        if (primitiveClass.equals(short.class)) {
            return Short.class;
        }
        if (primitiveClass.equals(short[].class)) {
            return Short[].class;
        }
        if (primitiveClass.equals(void.class)) {
            return Void.class;
        }
        if (!primitiveClass.isPrimitive()) {
            return primitiveClass;
        }
        return null;
    }
}
