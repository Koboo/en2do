package eu.koboo.en2do.mongodb.exception.methods;

import java.lang.reflect.Method;

public class MethodParameterCountException extends Exception {

    public MethodParameterCountException(Method method, Class<?> repoClass, int expected, int length) {
        super("Mismatching count of parameters in \"" + method.getName() + "\" of " + repoClass.getName() + "! " +
            "(expected=" + expected + ", actual=" + length + ")");
    }
}
