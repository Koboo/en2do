package eu.koboo.en2do.repository.methods.options.sort.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(value = SortByArray.class)
public @interface SortBy {

    String field();

    boolean ascending() default false;
}
