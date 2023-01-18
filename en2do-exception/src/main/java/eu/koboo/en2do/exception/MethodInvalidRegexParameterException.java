package eu.koboo.en2do.exception;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class MethodInvalidRegexParameterException extends Exception {

    public MethodInvalidRegexParameterException(Method method, Class<?> repoClass, Class<?> paramClass) {
        super("Invalid regex parameter on " + method.getName() + " in " + repoClass.getName() + "! " +
                "Only " + String.class.getName() + " and " + Pattern.class.getName() + " allowed. " +
                "(param=" + paramClass + ")");
    }
}
