package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryGetterNotFoundException extends RepositoryTypeException {

    public RepositoryGetterNotFoundException(Class<?> typeClass, Class<?> repositoryClass, String fieldName) {
        super("No getter method for the field \"" + fieldName + "\"!",
            repositoryClass, typeClass);
    }
}
