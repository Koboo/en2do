package eu.koboo.en2do.mongodb.exception.returntype;

import java.lang.reflect.Method;

public class MethodFindListTypeException extends Exception {

    public MethodFindListTypeException(Method method, Class<?> repoClass, Class<?> listType, Class<?> entityClass) {
        super("Method \"" + method.getName() + "\" of the repository " + repoClass.getName() + " returns a List, which uses the type " +
            listType.getName() + ", but it's only allowed to use the entity type " + entityClass.getName() + "!");
    }
}
