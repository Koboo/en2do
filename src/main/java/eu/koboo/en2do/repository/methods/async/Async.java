package eu.koboo.en2do.repository.methods.async;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a method as asynchronous.
 * If this annotation is used, the method needs to return a CompletableFuture,
 * with the type it normally returns.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Async {
}