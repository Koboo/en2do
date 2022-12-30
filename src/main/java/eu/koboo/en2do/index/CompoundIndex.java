package eu.koboo.en2do.index;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CompoundIndexArray.class)
public @interface CompoundIndex {

    Index[] value();

    boolean uniqueIndex() default false;
}
