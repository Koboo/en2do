package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class MethodInvalidRegexParameterException extends Exception {

    public MethodInvalidRegexParameterException(Method method, Class<?> repoClass, Class<?> paramClass) {
        super("Invalid regex parameter found in method \"" + method.getName() + "\" of the repository " + repoClass.getName() + "! " +
            "Only regex parameters of type \"" + String.class.getSimpleName() + "\" or \"" +
            Pattern.class.getSimpleName() + "\" are allowed, but used type is \"" + paramClass + "\".");
    }
}
