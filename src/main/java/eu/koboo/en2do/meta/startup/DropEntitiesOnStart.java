package eu.koboo.en2do.meta.startup;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DropEntitiesOnStart {
}
