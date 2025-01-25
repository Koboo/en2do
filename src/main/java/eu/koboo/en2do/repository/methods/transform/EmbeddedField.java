package eu.koboo.en2do.repository.methods.transform;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(EmbeddedField.EmbeddedKeyArray.class)
public @interface EmbeddedField {

    String key();

    String query();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface EmbeddedKeyArray {

        EmbeddedField[] value();
    }
}
