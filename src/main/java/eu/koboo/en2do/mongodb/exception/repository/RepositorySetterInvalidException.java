package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositorySetterInvalidException extends RepositoryTypeException {

    public RepositorySetterInvalidException(Class<?> typeClass, Class<?> repositoryClass, String fieldName) {
        super("Invalid setter method for the field \"" + fieldName + "\"! " +
            "It's needs to be public and match the parameter count of 1.", repositoryClass, typeClass);
    }
}
