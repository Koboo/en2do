package eu.koboo.en2do.test.utilstest;

import eu.koboo.en2do.utility.NameUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameUtilsTests {

    @Test
    public void testToCamelCaseLower() {
        String expectedOutput = "myCamelCase";
        assertEquals(expectedOutput, NameUtils.toCamelCaseLower("myCamelCase"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseLower("MyCamelCase"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseLower("my camel case"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseLower("my camel-case"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseLower("my_camel-case"));
    }

    @Test
    public void testToCamelCaseUpper() {
        String expectedOutput = "MyCamelCase";
        assertEquals(expectedOutput, NameUtils.toCamelCaseUpper("myCamelCase"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseUpper("MyCamelCase"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseUpper("My camel case"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseUpper("my camel-case"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseUpper("my_camel-case"));
        assertEquals(expectedOutput, NameUtils.toCamelCaseUpper("My_camel-Case"));
    }

    @Test
    public void testToSnakeCase() {
        String expectedOutput = "my_snake_case";
        assertEquals(expectedOutput, NameUtils.toSnakeCase("MySnakeCase"));
        assertEquals(expectedOutput, NameUtils.toSnakeCase("mySnakeCase"));
        assertEquals(expectedOutput, NameUtils.toSnakeCase("my Snake-case"));
        assertEquals(expectedOutput, NameUtils.toSnakeCase("my snake case"));
        assertEquals(expectedOutput, NameUtils.toSnakeCase("My_Snake_Case"));
    }
}
