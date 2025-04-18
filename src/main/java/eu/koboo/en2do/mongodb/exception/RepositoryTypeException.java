package eu.koboo.en2do.mongodb.exception;

public class RepositoryTypeException extends RepositoryException {

    public RepositoryTypeException(String message, Class<?> repository,
                                   Class<?> type, Throwable cause) {
        super(buildMessage(message, type), repository, cause);
    }

    public RepositoryTypeException(String message, Class<?> repository, Class<?> type) {
        super(buildMessage(message, type), repository);
    }

    private static String buildMessage(String message, Class<?> type) {
        return message + "\n" +
            "  - TypeClass: " + type.getName();
    }
}
