package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodFindListReturnTypeException extends Exception {

    public MethodFindListReturnTypeException(Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Methods, which return a list of the entity " + entityClass.getName() + ", " +
                "have to start with the keyword \"findManyBy\"! Please correct the method \"" + method.getName() +
                "\" of the repository " + repoClass.getName() + ".");
    }
}
