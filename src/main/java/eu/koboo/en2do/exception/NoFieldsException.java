package eu.koboo.en2do.exception;

public class NoFieldsException extends Exception {

    public NoFieldsException(Class<?> entityClass) {
        super("No fields found in " + entityClass.getName() + "!");
    }
}