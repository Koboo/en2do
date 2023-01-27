package eu.koboo.en2do.repository;

import java.lang.annotation.*;

/**
 * This annotation defines the collection name of the repository in the mongodb.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Collection {

    /**
     * @return The collection name in the mongodb.
     */
    String value();
}
