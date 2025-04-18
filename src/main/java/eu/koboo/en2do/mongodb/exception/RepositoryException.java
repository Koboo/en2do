package eu.koboo.en2do.mongodb.exception;

public class RepositoryException extends RuntimeException {

    public RepositoryException(String message, Class<?> repository, Throwable cause) {
        super(buildMessage(message, repository), cause);
    }

    public RepositoryException(String message, Class<?> repository) {
        super(buildMessage(message, repository));
    }

    private static String buildMessage(String message, Class<?> repository) {
        return message + "\n" +
            "  - Repository: " + repository.getName();
    }
}
