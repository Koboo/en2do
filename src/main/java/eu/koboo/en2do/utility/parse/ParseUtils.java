package eu.koboo.en2do.utility.parse;

import eu.koboo.en2do.utility.reflection.PrimitiveUtils;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class ParseUtils {

    public ParameterizedType decapsulateFuture(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!CompletableFuture.class.isAssignableFrom(returnType)) {
            if (Collection.class.isAssignableFrom(returnType)) {
                return (ParameterizedType) method.getGenericReturnType();
            }
            return null;
        }
        ParameterizedType futureGenericType = (ParameterizedType) method.getGenericReturnType();
        Type innerFutureType = futureGenericType.getActualTypeArguments()[0];
        if (!(innerFutureType instanceof ParameterizedType)) {
            return futureGenericType;
        }
        return (ParameterizedType) innerFutureType;
    }

    public Class<?> parseValidatableReturnType(Method method) {
        // Need to parse these 2 return types:
        // - "boolean exists(...)"
        // - "CompletableFuture<Boolean> exists(...)"
        ParameterizedType parameterizedType = decapsulateFuture(method);
        Class<?> entityTypeClass;
        if (parameterizedType == null) {
            entityTypeClass = method.getReturnType();
        } else {
            entityTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        return PrimitiveUtils.toWrapper(entityTypeClass);
    }
}
