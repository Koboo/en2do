package eu.koboo.en2do.repository;

import java.lang.annotation.*;

/**
 * This annotation appends the repository method name to the mongodb find queries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AppendMethodAsComment {
}
