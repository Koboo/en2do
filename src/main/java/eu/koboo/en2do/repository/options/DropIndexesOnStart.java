package eu.koboo.en2do.repository.options;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DropIndexesOnStart {
}
