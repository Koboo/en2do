package eu.koboo.en2do.repository.entity;

import java.lang.annotation.*;

/**
 * This annotation disables the creating of the unique index of the "@Id" field.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/index/identifier-index">...</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NonIndex {
}
