package eu.koboo.en2do.internal.exception.methods;

import eu.koboo.en2do.repository.methods.fields.UpdateBatch;

import java.lang.reflect.Method;

public class MethodBatchNotAllowedException extends Exception {

    public MethodBatchNotAllowedException(Method method, Class<?> repoClass) {
        super("The method \"" + method.getName() + "\" of repository " + repoClass.getName() + " is not allowed to " +
              "have the parameter " + UpdateBatch.class + "! " +
              "It's only allowed in methods, which start with the operator \"updateFieldsBy\".");
    }
}
