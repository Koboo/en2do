package eu.koboo.en2do.repository.methods.transform;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(NestedBsonKey.NestedBsonKeyArray.class)
public @interface NestedBsonKey {

    String id();

    String bson();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface NestedBsonKeyArray {

        NestedBsonKey[] value();
    }
}
