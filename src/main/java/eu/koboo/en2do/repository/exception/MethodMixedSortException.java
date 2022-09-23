package eu.koboo.en2do.repository.exception;

import eu.koboo.en2do.sort.Sort;
import eu.koboo.en2do.sort.annotation.SortBy;

import java.lang.reflect.Method;

public class MethodMixedSortException extends Exception {

    public MethodMixedSortException(Method method, Class<?> repoClass) {
        super("Method " + method.getName() + " in " + repoClass.getName() + " mixed sorting with object " +
                Sort.class + " and annotation " + SortBy.class + "! That's not allowed.");
    }
}
