package eu.koboo.en2do.internal.exception.repository;

import java.lang.reflect.Field;

public class RepositoryFinalFieldException extends Exception {

    public RepositoryFinalFieldException(Field field, Class<?> repoClass) {
        super("Field modifier final found on field \"" + field.getName() + "\" in entity of " +
                repoClass.getName() + ". That's not allowed!");
    }
}
