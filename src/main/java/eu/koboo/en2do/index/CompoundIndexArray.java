package eu.koboo.en2do.index;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CompoundIndexArray {

    CompoundIndex[] value();
}
