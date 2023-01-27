package eu.koboo.en2do.repository.entity.compound;

import java.lang.annotation.*;

/**
 * This annotation is used to create an array of the @CompoundIndex annotation.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CompoundIndexArray {

    /**
     * @return The array of the @CompoundIndex annotations.
     */
    CompoundIndex[] value();
}
