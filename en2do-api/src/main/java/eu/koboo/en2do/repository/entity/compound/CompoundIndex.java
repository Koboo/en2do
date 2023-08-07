package eu.koboo.en2do.repository.entity.compound;

import java.lang.annotation.*;

/**
 * This annotation is used to create new indexes for the repository.
 * If the CompoundIndex is set as "uniqueIndex = true" and two documents contain the same
 * values in indexed fields, an exception is thrown by mongodb-driver.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/index/compound-index-multi-field-index">...</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CompoundIndexArray.class)
public @interface CompoundIndex {

    /**
     * An array of all indexed field in this compound index.
     *
     * @return The array with the index annotations.
     */
    Index[] value();

    /**
     * Defines if the index array is unique. This speeds up the usage of queries on the indexed fields drastically.
     *
     * @return true, if the compound index is unique on every document.
     */
    boolean uniqueIndex() default false;
}
