package eu.koboo.en2do.repository.entity.compound;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CompoundIndex.class)
public @interface Index {

    String value();

    boolean ascending() default true;
}
