package eu.koboo.en2do.methods.registry;

import java.lang.reflect.Method;

public interface MethodHandler<E> {

    Object handle(Method method, Object[] arguments) throws Exception;
}
