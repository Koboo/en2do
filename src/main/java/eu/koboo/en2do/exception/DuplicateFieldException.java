package eu.koboo.en2do.exception;

import java.lang.reflect.Field;

public class DuplicateFieldException extends Exception {

    public DuplicateFieldException(Field field, Class<?> entityClass) {
        super("Duplicated fields with name \"" + field.getName() + "\" in " + entityClass.getName() + "!");
    }
}