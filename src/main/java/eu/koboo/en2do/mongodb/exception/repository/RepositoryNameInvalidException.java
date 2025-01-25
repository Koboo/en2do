package eu.koboo.en2do.mongodb.exception.repository;

public class RepositoryNameInvalidException extends Exception {

    public RepositoryNameInvalidException(Class<?> repositoryClass, String regex, String collectionName) {
        super("Invalid collection name! \n" +
            "  - Repository: " + repositoryClass.getName() + "\n" +
            "  - Checked regex: " + regex + "\n" +
            "  - Invalid name: " + collectionName + "\n"
        );
    }
}
