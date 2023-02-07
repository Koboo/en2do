package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodMismatchingTypeException extends Exception {

    public MethodMismatchingTypeException(Method method, Class<?> repoClass, Class<?> fieldClass, Class<?> paramClass) {
        super("The field in the entity is type \"" + fieldClass.getSimpleName() + "\" and the parameter type is " +
              "\"" + paramClass.getSimpleName() + "\". Mismatching parameter types in \"" + method.getName() + "\" " +
              "of repository " + repoClass.getName() + ".");
    }
}
