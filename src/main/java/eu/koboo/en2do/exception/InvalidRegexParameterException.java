package eu.koboo.en2do.exception;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class InvalidRegexParameterException extends Exception {

    public InvalidRegexParameterException(Method method, Class<?> entityClass) {
        super("Invalid regex parameter on " + method.getName() + " in " + entityClass.getName() + "! " +
                "Only " + String.class.getName() + " and " + Pattern.class.getName() + " allowed.");
    }
}