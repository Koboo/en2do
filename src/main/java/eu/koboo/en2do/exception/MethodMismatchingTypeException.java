package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodMismatchingTypeException extends Exception {

    public MethodMismatchingTypeException(Method method, Class<?> repoClass, Class<?> fieldClass, Class<?> paramClass) {
        super("Mismatching parameter type in " + method.getName() + " of " + repoClass.getName() + ", because " +
                "field=" + fieldClass.getName() + " but parameter=" + paramClass.getName() + "!");
    }
}