package eu.koboo.en2do.repository.exception;

public class MethodUnsupportedReturnTypeException extends Exception {

    public MethodUnsupportedReturnTypeException(Class<?> returnTypeClass, String methodName, Class<?> repoClass) {
        super("Unsupported return-type " + returnTypeClass.getName() + " at method " +
                methodName + " in " + repoClass.getName());
    }
}