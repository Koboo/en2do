package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodDeleteReturnTypeException extends Exception {

    public MethodDeleteReturnTypeException(Method method, Class<?> repoClass) {
        super("Methods, which return " + Boolean.class.getName() + " has to start with keyword \"deleteBy\"! " +
                "Please correct the method " + method.getName() + " in " + repoClass.getName() + ".");
    }
}