package eu.koboo.en2do.repository.entity.compound;

import java.lang.annotation.*;

/**
 * This annotation is used to index the field by its name.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/index/compound-index-multi-field-index">...</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CompoundIndex.class)
public @interface Index {

    /**
     * Sets the name of the field, which should be used to index.
     * @return The field name, which should be indexed.
     */
    String value();

    /**
     * Sets the direction of the index.
     * @return true, if the direction is ascending. false, if the direction is descending.
     */
    boolean ascending() default true;
}
