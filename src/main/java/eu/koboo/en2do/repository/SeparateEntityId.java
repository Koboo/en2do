package eu.koboo.en2do.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation separates the "_id" field of the mongodb document and the "@Id" annotated field of the
 * entity. If this annotation is NOT used on the repository, the "@Id" field can't be annotated with "@NonIndex", that
 * will throw an exception.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SeparateEntityId {
}