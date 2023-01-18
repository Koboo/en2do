package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodFindListReturnTypeException extends Exception {

    public MethodFindListReturnTypeException(Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Methods, which return a list of " + entityClass.getName() + ", " +
                "has to start with keyword \"findManyBy\"! Please correct the method " + method.getName() +
                " in " + repoClass.getName() + ".");
    }
}
