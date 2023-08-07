package eu.koboo.en2do.repository.methods.sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to limit the amount of the returned entities in the List.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/sorting/sorting-by-annotation">...</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Limit {

    /**
     * @return The amount of the entities in the returned List.
     */
    int value();
}
