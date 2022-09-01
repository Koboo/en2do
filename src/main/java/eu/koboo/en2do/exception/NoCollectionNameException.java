package eu.koboo.en2do.exception;

public class NoCollectionNameException extends Exception {

    public NoCollectionNameException(Class<?> repoClass) {
        super("No or invalid collection name given through @Repository annotation in " + repoClass.getName() + "!");
    }
}