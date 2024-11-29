package eu.koboo.en2do.mongodb.exception.returntype;

import java.lang.reflect.Method;

public class MethodFilterReturnTypeException extends Exception {

    public MethodFilterReturnTypeException(Method method, Class<?> entityClass, Class<?> repoClass) {
        super("Methods, which return a entity of type " + entityClass.getName() + ", " +
            "have to start with keyword \"filterBy\"! Please correct the method \"" + method.getName() +
            "\" of the repository " + repoClass.getName() + ".");
    }
}
