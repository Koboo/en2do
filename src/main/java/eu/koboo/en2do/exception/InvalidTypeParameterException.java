package eu.koboo.en2do.exception;

public class InvalidTypeParameterException extends Exception {

    public InvalidTypeParameterException(Class<?> repoClass) {
        super("Invalid type-parameters found in repository " + repoClass.getName() + ".");
    }
}