package eu.koboo.en2do.index;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CompoundIndex.class)
public @interface Index {

    String value();

    boolean ascending() default true;
}
