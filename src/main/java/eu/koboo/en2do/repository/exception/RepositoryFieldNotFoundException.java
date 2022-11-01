package eu.koboo.en2do.repository.exception;

public class RepositoryFieldNotFoundException extends Exception {

    public RepositoryFieldNotFoundException(Class<?> repoClass, String indexName) {
        super("Couldn't find " + indexName + " field in entity of " + repoClass.getName() + " to index.");
    }
}
