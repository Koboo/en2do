package eu.koboo.en2do.repository.entity.ttl;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TTLIndexArray {

    TTLIndex[] value();
}
