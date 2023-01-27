package eu.koboo.en2do.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation appends the repository method name to the mongodb find queries.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/repository-options/appendmethodascomment">...</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AppendMethodAsComment {
}
