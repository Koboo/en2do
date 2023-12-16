package eu.koboo.en2do.repository.methods.sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to skip the defined amount of entities by the given sorting.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/sorting/sorting-by-annotation">...</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Skip {

    /**
     * @return The amount of the skipped entities.
     */
    int value();
}
