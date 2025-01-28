package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryGetterInvalidException extends RepositoryTypeException {

    public RepositoryGetterInvalidException(Class<?> typeClass, Class<?> repositoryClass, String fieldName) {
        super("Invalid getter method for the field \"" + fieldName + "\"! " +
            "It's needs to be public and match the parameter count of 0.", repositoryClass, typeClass);
    }
}
