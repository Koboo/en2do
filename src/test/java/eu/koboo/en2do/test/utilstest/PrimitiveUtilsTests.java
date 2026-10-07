package eu.koboo.en2do.test.utilstest;

import eu.koboo.en2do.utility.reflection.PrimitiveUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveUtilsTests {

    @Test
    public void testWrapperOfInt() {
        assertEquals(Integer.class, PrimitiveUtils.toWrapper(int.class));
        assertEquals(Integer.class, PrimitiveUtils.toWrapper(Integer.class));
        assertEquals(Integer[].class, PrimitiveUtils.toWrapper(int[].class));
    }

    @Test
    public void testWrapperOfLong() {
        assertEquals(Long.class, PrimitiveUtils.toWrapper(long.class));
        assertEquals(Long.class, PrimitiveUtils.toWrapper(Long.class));
        assertEquals(Long[].class, PrimitiveUtils.toWrapper(long[].class));
    }

    @Test
    public void testWrapperOfBoolean() {
        assertEquals(Boolean.class, PrimitiveUtils.toWrapper(boolean.class));
        assertEquals(Boolean.class, PrimitiveUtils.toWrapper(Boolean.class));
        assertEquals(Boolean[].class, PrimitiveUtils.toWrapper(boolean[].class));
    }

    @Test
    public void testWrapperOfByte() {
        assertEquals(Byte.class, PrimitiveUtils.toWrapper(byte.class));
        assertEquals(Byte.class, PrimitiveUtils.toWrapper(Byte.class));
        assertEquals(Byte[].class, PrimitiveUtils.toWrapper(byte[].class));
    }

    @Test
    public void testWrapperOfChar() {
        assertEquals(Character.class, PrimitiveUtils.toWrapper(char.class));
        assertEquals(Character.class, PrimitiveUtils.toWrapper(Character.class));
        assertEquals(Character[].class, PrimitiveUtils.toWrapper(char[].class));
    }

    @Test
    public void testWrapperOfFloat() {
        assertEquals(Float.class, PrimitiveUtils.toWrapper(float.class));
        assertEquals(Float.class, PrimitiveUtils.toWrapper(Float.class));
        assertEquals(Float[].class, PrimitiveUtils.toWrapper(float[].class));
    }

    @Test
    public void testWrapperOfDouble() {
        assertEquals(Double.class, PrimitiveUtils.toWrapper(double.class));
        assertEquals(Double.class, PrimitiveUtils.toWrapper(Double.class));
        assertEquals(Double[].class, PrimitiveUtils.toWrapper(double[].class));
    }

    @Test
    public void testWrapperOfShort() {
        assertEquals(Short.class, PrimitiveUtils.toWrapper(short.class));
        assertEquals(Short.class, PrimitiveUtils.toWrapper(Short.class));
        assertEquals(Short[].class, PrimitiveUtils.toWrapper(short[].class));
    }

    @Test
    public void testWrapperOfVoid() {
        assertEquals(Void.class, PrimitiveUtils.toWrapper(void.class));
        assertEquals(Void.class, PrimitiveUtils.toWrapper(Void.class));
    }
}
