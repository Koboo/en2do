package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryException;

public class RepositoryNameDuplicateException extends RepositoryException {

    public RepositoryNameDuplicateException(Class<?> repository, String collectionName) {
        super("Duplicated collection name given! \n" +
            "  - Collection: " + collectionName, repository);
    }
}
