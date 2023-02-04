package eu.koboo.en2do.internal.exception.methods;

import eu.koboo.en2do.repository.methods.fields.UpdateBatch;

import java.lang.reflect.Method;

public class MethodBatchNotAllowedException extends Exception {

    public MethodBatchNotAllowedException(Method method, Class<?> repoClass) {
        super("The method " + method.getName() + " in " +
                repoClass.getName() + " is not allowed to have the parameter " + UpdateBatch.class + "!");
    }
}
