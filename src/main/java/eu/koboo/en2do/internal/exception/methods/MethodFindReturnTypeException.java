package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodFindReturnTypeException extends Exception {

    public MethodFindReturnTypeException(Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Methods, which return a entity of type " + entityClass.getName() + ", " +
              "have to start with keyword \"findFirstBy\"! Please correct the method \"" + method.getName() +
              "\" of the repository " + repoClass.getName() + ".");
    }
}
