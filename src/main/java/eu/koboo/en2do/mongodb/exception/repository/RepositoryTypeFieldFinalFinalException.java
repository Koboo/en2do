package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

import java.lang.reflect.Field;

public class RepositoryTypeFieldFinalFinalException extends RepositoryTypeException {

    public RepositoryTypeFieldFinalFinalException(Class<?> typeClass, Class<?> repositoryClass, Field field) {
        super("Field modifier final found on field \"" + field.getName() + "\"! That's not allowed!",
            repositoryClass, typeClass);
    }
}
