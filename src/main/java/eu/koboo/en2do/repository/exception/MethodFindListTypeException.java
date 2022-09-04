package eu.koboo.en2do.repository.exception;

import java.lang.reflect.Method;

public class MethodFindListTypeException extends Exception {

    public MethodFindListTypeException(Method method, Class<?> repoClass, Class<?> listType) {
        super("Method " + method.getName() + " in " + repoClass.getName() + " returns a list with type " +
                listType.getName() + " but requires the entity!");
    }
}