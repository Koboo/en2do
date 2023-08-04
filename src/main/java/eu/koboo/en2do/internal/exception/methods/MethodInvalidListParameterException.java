package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodInvalidListParameterException extends Exception {

    public MethodInvalidListParameterException(Method method, Class<?> repoClass, Class<?> fieldClass, Class<?> paramClass) {
        super("The List parameter of the filter method \"In\" uses invalid types! The List uses type \"" + paramClass.getName() + "\", " +
                "but expected a List using type \"" + fieldClass.getName() + "\". Please check the method \"" + method.getName() + "\" " +
                "of the repository " + repoClass.getName() + ".");
    }
}
