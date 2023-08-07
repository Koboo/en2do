package eu.koboo.en2do.repository.entity;

import java.lang.annotation.*;

/**
 * This annotation defines the unique identifier of the entity.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/index/identifier-index">...</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
}
