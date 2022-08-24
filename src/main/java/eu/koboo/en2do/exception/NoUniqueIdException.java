package eu.koboo.en2do.exception;

public class NoUniqueIdException extends Exception {

    public NoUniqueIdException() {
    }

    public NoUniqueIdException(String message) {
        super(message);
    }

    public NoUniqueIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoUniqueIdException(Throwable cause) {
        super(cause);
    }

    public NoUniqueIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}