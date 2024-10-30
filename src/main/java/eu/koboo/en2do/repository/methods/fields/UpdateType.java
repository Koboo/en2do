package eu.koboo.en2do.repository.methods.fields;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Represents the type of the update behaviour.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum UpdateType {

    /**
     * Sets the value of the given field to the given value.
     * Note: Overrides the previous values.
     */
    SET("$set"),
    /**
     * Renames the given field to the given new field name.
     */
    RENAME("$rename"),
    /**
     * Removes the field from every document.
     */
    REMOVE("$unset");

    String documentType;
}
