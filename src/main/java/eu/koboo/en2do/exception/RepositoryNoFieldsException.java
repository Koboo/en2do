package eu.koboo.en2do.exception;

public class RepositoryNoFieldsException extends Exception {

    public RepositoryNoFieldsException(Class<?> repoClass) {
        super("No fields found in entity of " + repoClass.getName() + "!");
    }
}
