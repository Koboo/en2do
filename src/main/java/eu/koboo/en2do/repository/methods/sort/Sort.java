package eu.koboo.en2do.repository.methods.sort;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * This object is used to enable dynamic sorting, without defining the sorting options statically through annotations.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/sorting/sorting-by-parameter">...</a>
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class Sort {

    public static Sort create() {
        return new Sort();
    }

    public static Sort byField(String fieldName) {
        return new Sort().field(fieldName);
    }

    public static Sort byField(String fieldName, boolean ascending) {
        return new Sort().field(fieldName, ascending);
    }

    String fieldName;
    boolean ascending;
    int limit;
    int skip;

    private Sort() {
        this.fieldName = null;
        this.ascending = true;
        this.limit = -1;
        this.skip = -1;
    }

    /**
     * Use this method to define the order / sorting by the given fields,
     * but sets the ascending value to "true"
     *
     * @param fieldName The field which is used to sort
     * @return The used Sort object
     */
    public Sort field(String fieldName, boolean ascending) {
        this.fieldName = fieldName;
        this.ascending = ascending;
        return this;
    }

    /**
     * Use this method to define the order / sorting by the given fields,
     * but sets the ascending value to "true"
     *
     * @param fieldName The field which is used to sort
     * @return The used Sort object
     */
    public Sort field(String fieldName) {
        return field(fieldName, true);
    }

    /**
     * Use this method to set the limit of entities of the current sorting.
     *
     * @param limit The amount of the entities in the returned List.
     * @return The used Sort object
     */
    public Sort limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Use this method to set the skipped entities of the current sorting.
     *
     * @param skip The amount of the entities, which should be skipped, before creating the List.
     * @return The used Sort object
     */
    public Sort skip(int skip) {
        this.skip = skip;
        return this;
    }
}
