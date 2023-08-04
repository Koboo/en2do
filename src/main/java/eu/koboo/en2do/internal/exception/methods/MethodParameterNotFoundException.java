package eu.koboo.en2do.internal.exception.methods;

import java.lang.reflect.Method;

public class MethodParameterNotFoundException extends Exception {

    public MethodParameterNotFoundException(Method method, Class<?> repoClass, int index, int totalParams) {
        super("Couldn't find parameter in \"" + method.getName() + "\" of " + repoClass.getName() + " at index=" + index + "," +
                "because length=" + totalParams + "!");
    }
}
