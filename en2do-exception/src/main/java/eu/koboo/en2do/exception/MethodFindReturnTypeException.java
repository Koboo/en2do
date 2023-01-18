package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodFindReturnTypeException extends Exception {

    public MethodFindReturnTypeException(Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Methods, which return a " + entityClass.getName() + ", " +
                "has to start with keyword \"findFirstBy\"! Please correct the method " + method.getName() +
                " in " + repoClass.getName() + ".");
    }
}
