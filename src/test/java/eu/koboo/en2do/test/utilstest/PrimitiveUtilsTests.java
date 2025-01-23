package eu.koboo.en2do.test.utilstest;

import eu.koboo.en2do.utility.PrimitiveUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveUtilsTests {

    @Test
    public void testWrapperOfInt() {
        assertEquals(Integer.class, PrimitiveUtils.wrapperOf(int.class));
        assertEquals(Integer.class, PrimitiveUtils.wrapperOf(Integer.class));
        assertEquals(Integer[].class, PrimitiveUtils.wrapperOf(int[].class));
    }

    @Test
    public void testWrapperOfLong() {
        assertEquals(Long.class, PrimitiveUtils.wrapperOf(long.class));
        assertEquals(Long.class, PrimitiveUtils.wrapperOf(Long.class));
        assertEquals(Long[].class, PrimitiveUtils.wrapperOf(long[].class));
    }

    @Test
    public void testWrapperOfBoolean() {
        assertEquals(Boolean.class, PrimitiveUtils.wrapperOf(boolean.class));
        assertEquals(Boolean.class, PrimitiveUtils.wrapperOf(Boolean.class));
        assertEquals(Boolean[].class, PrimitiveUtils.wrapperOf(boolean[].class));
    }

    @Test
    public void testWrapperOfByte() {
        assertEquals(Byte.class, PrimitiveUtils.wrapperOf(byte.class));
        assertEquals(Byte.class, PrimitiveUtils.wrapperOf(Byte.class));
        assertEquals(Byte[].class, PrimitiveUtils.wrapperOf(byte[].class));
    }

    @Test
    public void testWrapperOfChar() {
        assertEquals(Character.class, PrimitiveUtils.wrapperOf(char.class));
        assertEquals(Character.class, PrimitiveUtils.wrapperOf(Character.class));
        assertEquals(Character[].class, PrimitiveUtils.wrapperOf(char[].class));
    }

    @Test
    public void testWrapperOfFloat() {
        assertEquals(Float.class, PrimitiveUtils.wrapperOf(float.class));
        assertEquals(Float.class, PrimitiveUtils.wrapperOf(Float.class));
        assertEquals(Float[].class, PrimitiveUtils.wrapperOf(float[].class));
    }

    @Test
    public void testWrapperOfDouble() {
        assertEquals(Double.class, PrimitiveUtils.wrapperOf(double.class));
        assertEquals(Double.class, PrimitiveUtils.wrapperOf(Double.class));
        assertEquals(Double[].class, PrimitiveUtils.wrapperOf(double[].class));
    }

    @Test
    public void testWrapperOfShort() {
        assertEquals(Short.class, PrimitiveUtils.wrapperOf(short.class));
        assertEquals(Short.class, PrimitiveUtils.wrapperOf(Short.class));
        assertEquals(Short[].class, PrimitiveUtils.wrapperOf(short[].class));
    }

    @Test
    public void testWrapperOfVoid() {
        assertEquals(Void.class, PrimitiveUtils.wrapperOf(void.class));
        assertEquals(Void.class, PrimitiveUtils.wrapperOf(Void.class));
    }
}
