package eu.koboo.en2do.test.utilstest;

import eu.koboo.en2do.utility.NameCasingUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameCasingUtilsTests {

    @Test
    public void testToCamelCaseLower() {
        String expectedOutput = "myCamelCase";
        assertEquals(expectedOutput, NameCasingUtils.toCamelCase("myCamelCase"));
        assertEquals(expectedOutput, NameCasingUtils.toCamelCase("MyCamelCase"));
        assertEquals(expectedOutput, NameCasingUtils.toCamelCase("my camel case"));
        assertEquals(expectedOutput, NameCasingUtils.toCamelCase("my camel-case"));
        assertEquals(expectedOutput, NameCasingUtils.toCamelCase("my_camel-case"));
    }

    @Test
    public void testToCamelCaseUpper() {
        String expectedOutput = "MyCamelCase";
        assertEquals(expectedOutput, NameCasingUtils.toPascalCase("myCamelCase"));
        assertEquals(expectedOutput, NameCasingUtils.toPascalCase("MyCamelCase"));
        assertEquals(expectedOutput, NameCasingUtils.toPascalCase("My camel case"));
        assertEquals(expectedOutput, NameCasingUtils.toPascalCase("my camel-case"));
        assertEquals(expectedOutput, NameCasingUtils.toPascalCase("my_camel-case"));
        assertEquals(expectedOutput, NameCasingUtils.toPascalCase("My_camel-Case"));
    }

    @Test
    public void testToSnakeCase() {
        String expectedOutput = "my_snake_case";
        assertEquals(expectedOutput, NameCasingUtils.toSnakeCase("MySnakeCase"));
        assertEquals(expectedOutput, NameCasingUtils.toSnakeCase("mySnakeCase"));
        assertEquals(expectedOutput, NameCasingUtils.toSnakeCase("my Snake-case"));
        assertEquals(expectedOutput, NameCasingUtils.toSnakeCase("my snake case"));
        assertEquals(expectedOutput, NameCasingUtils.toSnakeCase("My_Snake_Case"));
    }
}
