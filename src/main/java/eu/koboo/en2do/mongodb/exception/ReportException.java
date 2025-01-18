package eu.koboo.en2do.mongodb.exception;

public class ReportException extends RuntimeException {

    public ReportException(String message) {
        super(message +
            "Please report your code and other information to\n" +
            "https://github.com/Koboo/en2do\n" +
            "to ensure others don't get this bug and we can't look further into this issue.");
    }

}