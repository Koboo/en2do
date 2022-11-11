package eu.koboo.en2do.meta.registry;

import java.lang.reflect.Method;

public interface MethodHandler {

    Object handle(Method method, Object[] arguments) throws Exception;
}
