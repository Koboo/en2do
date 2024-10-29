package eu.koboo.en2do.mongodb.exception.repository;

public class RepositoryInvalidNameException extends Exception {

    public RepositoryInvalidNameException(Class<?> repoClass, Class<?> collectionClass, String collectionName) {
        super("Invalid collection name given through " + collectionClass.getName() + " annotation in " +
            repoClass.getName() + "! Invalid name is: \"" + collectionName + "\"");
    }
}
