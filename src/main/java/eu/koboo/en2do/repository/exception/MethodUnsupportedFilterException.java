package eu.koboo.en2do.repository.exception;

import java.lang.reflect.Method;

public class MethodUnsupportedFilterException extends Exception {

    public MethodUnsupportedFilterException(Method method, Class<?> repoClass) {
        super("Unsupported filter found on " + method.getName() + " in " + repoClass.getName() + "! " +
                "Please make sure to match the defined filter pattern!");
    }
}