package eu.koboo.en2do.exception;

import java.lang.reflect.Method;

public class MethodBooleanReturnTypeException extends Exception {

    public MethodBooleanReturnTypeException(Method method, Class<?> repoClass) {
        super("Methods, which return " + Boolean.class.getName() + " has to start with keywords \"deleteBy\" or " +
                "\"existsBy\"! Please correct the method " + method.getName() + " in " + repoClass.getName() + ".");
    }
}
