package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryException;

public class RepositoryNameEmptyException extends RepositoryException {

    public RepositoryNameEmptyException(Class<?> repository, String collectionName) {
        super("Empty collection name! \n" +
            "  - Invalid name: " + collectionName, repository);
    }
}
