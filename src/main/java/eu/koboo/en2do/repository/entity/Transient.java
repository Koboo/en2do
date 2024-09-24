package eu.koboo.en2do.repository.entity;

import java.lang.annotation.*;

/**
 * This annotation sets a field "ignored" by the database. All fields with this field, aren't saved to
 * the document of the entity. The value of the field will get lost, by saving and getting it from the database.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/transient">...</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Transient {
}
