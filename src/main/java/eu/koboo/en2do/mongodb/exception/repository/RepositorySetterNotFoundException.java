package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositorySetterNotFoundException extends RepositoryTypeException {

    public RepositorySetterNotFoundException(Class<?> typeClass, Class<?> repositoryClass, String fieldName) {
        super("No setter method for the field \"" + fieldName + "\"!",
            repositoryClass, typeClass);
    }
}
