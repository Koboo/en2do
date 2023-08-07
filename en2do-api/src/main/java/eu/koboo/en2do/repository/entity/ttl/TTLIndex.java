package eu.koboo.en2do.repository.entity.ttl;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * This annotation allows setting time-tp-live on specific fields of type "java.util.Date". MongoDB will delete
 * them by itself, so the application doesn't need a repeating task other something similar, to check for the time value
 * of the given field.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/index/ttl-index">...</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(TTLIndexArray.class)
public @interface TTLIndex {

    /**
     * Indicates, which field should be checked for the time-to-live index
     *
     * @return The name of the field in the entity
     */
    String value();

    /**
     * Sets the amount of the time unit
     *
     * @return The amount of time
     */
    long ttl() default 0;

    /**
     * Sets the unit of the time-to-live index
     *
     * @return The unit of the ttl value
     */
    TimeUnit time() default TimeUnit.SECONDS;
}
