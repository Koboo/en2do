package eu.koboo.en2do.repository.internal.methods.operators;

import java.lang.reflect.Method;

public interface ReturnTypeValidator {

    void check(Method method, Class<?> returnType, Class<?> entityClass, Class<?> repoClass) throws Exception;
}
