package eu.koboo.en2do.exception;

public class RepositoryNameNotFoundException extends Exception {

    public RepositoryNameNotFoundException(Class<?> repoClass) {
        super("No or invalid collection name given through @Repository annotation in " + repoClass.getName() + "! That's a required annotation.");
    }
}