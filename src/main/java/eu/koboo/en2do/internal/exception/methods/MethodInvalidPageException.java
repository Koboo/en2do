package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodInvalidPageException extends Exception {

    public MethodInvalidPageException(Method method, Class<?> repoClass) {
        super("You're not allowed to use a page size of less or equal to \"0\"!" +
                "You tried to execute the  method \"" + method.getName() + "\" of the repository " + repoClass.getName() + ". " +
                "Please check the execution parameters.");
    }
}
