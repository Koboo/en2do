package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeFieldInvalidException extends RepositoryTypeException {

    public RepositoryTypeFieldInvalidException(Class<?> typeClass, Class<?> repositoryClass) {
        super("Couldn't find any fields in typeClass!", repositoryClass, typeClass);
    }
}
