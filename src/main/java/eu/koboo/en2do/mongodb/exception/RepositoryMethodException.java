package eu.koboo.en2do.mongodb.exception;

import java.lang.reflect.Method;

public class RepositoryMethodException extends RepositoryException {

    public RepositoryMethodException(String message, Class<?> repository,
                                     Method method, Throwable cause) {
        super(buildMessage(message, method), repository, cause);
    }

    public RepositoryMethodException(String message, Class<?> repository, Method method) {
        super(buildMessage(message, method), repository);
    }

    private static String buildMessage(String message, Method method) {
        return message + "\n" +
            "  - Method: " + method.getName();
    }
}
