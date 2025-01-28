package eu.koboo.en2do.mongodb.exception.repository;

import eu.koboo.en2do.mongodb.exception.RepositoryException;

public class RepositoryNameInvalidException extends RepositoryException {

    public RepositoryNameInvalidException(Class<?> repositoryClass, String regex, String collectionName) {
        super("Invalid collection name! \n" +
            "  - Checked regex: " + regex + "\n" +
            "  - Invalid name: " + collectionName, repositoryClass);
    }
}
