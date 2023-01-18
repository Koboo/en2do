package eu.koboo.en2do.index.compound;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CompoundIndexArray {

    CompoundIndex[] value();
}
