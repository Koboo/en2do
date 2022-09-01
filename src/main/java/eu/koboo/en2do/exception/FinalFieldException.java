package eu.koboo.en2do.exception;

import java.lang.reflect.Field;

public class FinalFieldException extends Exception {

    public FinalFieldException(Field field, Class<?> entityClass) {
        super("Field \"" + field.getName() + "\" in " + entityClass.getName() + " is final. That's not allowed!");
    }
}