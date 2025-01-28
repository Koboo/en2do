package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryException;

public class RepositoryNameNotFoundException extends RepositoryException {

    public RepositoryNameNotFoundException(Class<?> repository) {
        super("No @collection annotation!", repository);
    }
}
