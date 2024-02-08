package eu.koboo.en2do.repository.entity.compound;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeoIndex {

    boolean sphere() default true;
}
