package eu.koboo.en2do.index;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntityIndexArray {

    EntityIndex[] value();
}
