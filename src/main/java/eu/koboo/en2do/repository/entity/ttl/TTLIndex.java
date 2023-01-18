package eu.koboo.en2do.repository.entity.ttl;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(TTLIndexArray.class)
public @interface TTLIndex {

    String value();

    long ttl() default 0;

    TimeUnit time() default TimeUnit.SECONDS;
}
