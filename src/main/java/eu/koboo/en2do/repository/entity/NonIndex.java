package eu.koboo.en2do.repository.entity;

import java.lang.annotation.*;

/**
 * This annotation disables the creating of the unique index of the "@Id" field.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NonIndex {
}
