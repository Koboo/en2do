package eu.koboo.en2do.exception;

import java.lang.reflect.Field;

public class RepositoryFinalFieldException extends Exception {

    public RepositoryFinalFieldException(Field field, Class<?> repositoryClass) {
        super("Field modifier final found on field \"" + field.getName() + "\" in entity of " + repositoryClass.getName() + ". That's not allowed!");
    }
}