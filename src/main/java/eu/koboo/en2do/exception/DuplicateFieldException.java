package eu.koboo.en2do.exception;

public class DuplicateFieldException extends Exception {

    public DuplicateFieldException() {
    }

    public DuplicateFieldException(String message) {
        super(message);
    }

    public DuplicateFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateFieldException(Throwable cause) {
        super(cause);
    }

    public DuplicateFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}