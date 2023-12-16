package eu.koboo.en2do.mongodb.exception.methods;

import java.lang.reflect.Method;

public class MethodInvalidSortLimitException extends Exception {

    public MethodInvalidSortLimitException(Method method, Class<?> repoClass) {
        super("You're not allowed to use a sorting limit of less or equal to \"0\"!" +
            "You tried to execute the  method \"" + method.getName() + "\" of the repository " + repoClass.getName() + ". " +
            "Please check the execution parameters.");
    }
}
