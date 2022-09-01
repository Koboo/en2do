package eu.koboo.en2do.exception;

public class NoUniqueIdException extends Exception {

    public NoUniqueIdException(Class<?> entityClass) {
        super("Class of UniqueId in " + entityClass.getName() + " not found!");
    }
}