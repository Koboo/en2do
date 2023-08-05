package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodMixedSortException extends Exception {

    public MethodMixedSortException(Method method, Class<?> repoClass, Class<?> sortObj, Class<?> sortAnno) {
        super("Method \"" + method.getName() + "\" of repository " + repoClass.getName() + " mixed sorting with object " +
            sortObj.getName() + " and annotation " + sortAnno.getName() + "! That's not allowed.");
    }
}
