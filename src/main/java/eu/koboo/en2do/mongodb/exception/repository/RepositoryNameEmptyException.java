package eu.koboo.en2do.mongodb.exception.repository;

public class RepositoryNameEmptyException extends Exception {

    public RepositoryNameEmptyException(Class<?> repository, String collectionName) {
        super("Empty collection name! \n" +
            "  - Repository: " + repository.getName() + "\n" +
            "  - Invalid name: " + collectionName + "\n"
        );
    }
}
