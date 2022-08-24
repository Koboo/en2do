package eu.koboo.en2do.exception;

public class NoFieldsException extends Exception {

    public NoFieldsException() {
    }

    public NoFieldsException(String message) {
        super(message);
    }

    public NoFieldsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoFieldsException(Throwable cause) {
        super(cause);
    }

    public NoFieldsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}