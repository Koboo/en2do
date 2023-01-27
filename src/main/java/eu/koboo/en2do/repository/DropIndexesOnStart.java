package eu.koboo.en2do.repository;

import java.lang.annotation.*;

/**
 * This annotation drops all previously created indexes, when the annotated repository is created.
 * CAUTION: This will delete all indexes permanently!
 * See documentation: <a href="https://koboo.gitbook.io/en2do/repository-options/dropindexesonstart">...</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DropIndexesOnStart {
}
