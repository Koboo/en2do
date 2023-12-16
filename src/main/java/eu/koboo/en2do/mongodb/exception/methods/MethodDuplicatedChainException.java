package eu.koboo.en2do.mongodb.exception.methods;

import java.lang.reflect.Method;

public class MethodDuplicatedChainException extends Exception {

    public MethodDuplicatedChainException(Method method, Class<?> repoClass) {
        super("It's not allowed to use \"And\" + \"Or\" operation in one method. Please look into the method \"" +
            method.getName() + "\" of the repository " + repoClass.getName() + ".");
    }
}
