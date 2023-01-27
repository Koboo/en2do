package eu.koboo.en2do.repository;

import java.lang.annotation.*;

/**
 * This annotation drops all previously created entities, when the annotated repository is created.
 * CAUTION: This will delete all entities permanently!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DropEntitiesOnStart {
}
