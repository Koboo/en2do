package eu.koboo.en2do.repository.methods.fields;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Represents a field change in all documents, which match the given filters.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldUpdate {

    /**
     * Set the defaultValue of a specific field.
     *
     * @param fieldName    The field, which should be set
     * @param defaultValue The default value, which gets set into the field.
     * @return The new created FieldUpdate instance.
     */
    public static FieldUpdate set(String fieldName, Object defaultValue) {
        return new FieldUpdate(UpdateType.SET, fieldName, defaultValue);
    }

    /**
     * Rename a specific field.
     *
     * @param fieldName The field, which should be renamed.
     * @param value     The new field name.
     * @return The new created FieldUpdate instance.
     */
    public static FieldUpdate rename(String fieldName, String value) {
        return new FieldUpdate(UpdateType.RENAME, fieldName, value);
    }

    /**
     * Remove a specific field.
     *
     * @param fieldName The field, which should be removed.
     * @return The new created FieldUpdate instance.
     */
    public static FieldUpdate remove(String fieldName) {
        return new FieldUpdate(UpdateType.REMOVE, fieldName, null);
    }

    UpdateType updateType;
    String fieldName;

    Object value;
}
