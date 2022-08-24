package eu.koboo.en2do.exception;

public class FinalFieldException extends Exception {

    public FinalFieldException() {
    }

    public FinalFieldException(String message) {
        super(message);
    }

    public FinalFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public FinalFieldException(Throwable cause) {
        super(cause);
    }

    public FinalFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}