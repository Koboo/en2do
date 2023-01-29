package eu.koboo.en2do.repository.methods.sort;

import java.lang.annotation.*;

/**
 * This annotation is used to define the sorting of the given fields.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/sorting/sorting-by-annotation">...</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(value = SortByArray.class)
public @interface SortBy {

    /**
     * @return The required field, which should be sorted.
     */
    String field();

    /**
     * @return The optional direction, which defaults to ascending = "true".
     */
    boolean ascending() default false;
}
