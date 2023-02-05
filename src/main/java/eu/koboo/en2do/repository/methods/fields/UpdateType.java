package eu.koboo.en2do.repository.methods.fields;

/**
 * Represents the type of the update behaviour.
 */
public enum UpdateType {

    /**
     * Sets the value of the given field to the given value.
     * Note: Overrides the previous values.
     */
    SET,
    /**
     * Renames the given field to the given new field name.
     */
    RENAME,
    /**
     * Removes the field from every document.
     */
    REMOVE
}
