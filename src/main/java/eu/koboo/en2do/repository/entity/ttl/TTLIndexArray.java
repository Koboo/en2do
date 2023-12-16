package eu.koboo.en2do.repository.entity.ttl;

import java.lang.annotation.*;

/**
 * This annotation is used to create an array of the @TTLIndex annotation.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TTLIndexArray {

    /**
     * @return The array of the @TTLIndex annotations.
     */
    TTLIndex[] value();
}
