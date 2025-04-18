package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryTypeException;

public class RepositoryTypeIdNotFoundException extends RepositoryTypeException {

    public RepositoryTypeIdNotFoundException(Class<?> repositoryClass, Class<?> entityClass) {
        super("Couldn't find @Id field in entity!", repositoryClass, entityClass);
    }
}
