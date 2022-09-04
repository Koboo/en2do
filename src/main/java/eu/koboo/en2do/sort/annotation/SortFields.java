package eu.koboo.en2do.sort.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SortFields {

    SortBy[] value() default {};
}