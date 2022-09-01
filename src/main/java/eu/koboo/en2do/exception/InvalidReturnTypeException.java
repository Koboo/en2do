package eu.koboo.en2do.exception;

public class InvalidReturnTypeException extends Exception {

    public InvalidReturnTypeException(Class<?> returnTypeClass, String methodName, Class<?> entityClass) {
        super("Invalid return-type " + returnTypeClass.getName() + " at method " + methodName + " in " + entityClass.getName());
    }
}