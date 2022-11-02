package eu.koboo.en2do.index;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(EntityIndexArray.class)
public @interface EntityIndex {

    String[] value();

    boolean ascending() default true;
}
