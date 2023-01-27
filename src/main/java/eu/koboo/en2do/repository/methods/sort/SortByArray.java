package eu.koboo.en2do.repository.methods.sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to create an array of the @SortBy annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SortByArray {

    /**
     * @return The array of the @SortBy annotations.
     */
    SortBy[] value();
}
