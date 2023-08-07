package eu.koboo.en2do.repository.entity;

import java.lang.annotation.*;

/**
 * This annotation "renames" a field in the mongodb document, to the name of the value attribute.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/transformfield">...</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransformField {

    /**
     * @return The name of the field in the document.
     */
    String value();
}
