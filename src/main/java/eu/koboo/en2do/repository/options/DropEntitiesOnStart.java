package eu.koboo.en2do.repository.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation drops all previously created entities, when the annotated repository is created.
 * CAUTION: This will delete all entities permanently!
 * See documentation: <a href="https://koboo.gitbook.io/en2do/repository-options/dropentitiesonstart">...</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DropEntitiesOnStart {
}
