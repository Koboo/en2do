package eu.koboo.en2do.repository.methods;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * This object is used to enable sorting and pagination,
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/sorting/sorting-by-parameter">...</a>
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public final class QueryOptions {

    public static QueryOptions byField(String fieldName) {
        return new QueryOptions().field(fieldName);
    }

    public static QueryOptions byField(String fieldName, boolean ascending) {
        return new QueryOptions().field(fieldName, ascending);
    }

    public static QueryOptions byPage(int page, int entitiesPerPage) {
        return new QueryOptions().page(page, entitiesPerPage);
    }

    String fieldName;
    boolean ascending;
    int limit;
    int skip;

    private QueryOptions() {
        this.fieldName = null;
        this.ascending = true;
        this.limit = -1;
        this.skip = -1;
    }

    /**
     * Use this method to define the order / sorting by the given fields,
     * and the direction using the ascending parameter
     *
     * @param fieldName The field which is used to sort
     * @param ascending The direction of sorting
     * @return this object
     */
    public QueryOptions field(String fieldName, boolean ascending) {
        this.fieldName = fieldName;
        this.ascending = ascending;
        return this;
    }

    /**
     * Use this to define the sorting direction by the given field.
     * The default ascending is true.
     *
     * @param fieldName The field which is used to sort
     * @return this object
     */
    public QueryOptions field(String fieldName) {
        return field(fieldName, true);
    }

    /**
     * Use this to set the amount of the limited entities.
     *
     * @param limit The maximum amount of the entities.
     * @return this object
     */
    public QueryOptions limit(int limit) {
        if(limit < 0) {
            throw new IllegalArgumentException("limit can not be negative.");
        }
        this.limit = limit;
        return this;
    }

    /**
     * Use this to set the amount of the skipped entities.
     *
     * @param skip The amount of the entities, which should be skipped
     * @return this object
     */
    public QueryOptions skip(int skip) {
        if(skip < 0) {
            throw new IllegalArgumentException("skip can not be negative.");
        }
        this.skip = skip;
        return this;
    }

    /**
     * Use this method to remove limiting entirely.
     *
     * @return this object
     */
    public QueryOptions removeLimit() {
        this.limit = -1;
        return this;
    }

    /**
     * Use this method to remove skipping entirely.
     *
     * @return this object
     */
    public QueryOptions removeSkip() {
        this.skip = -1;
        return this;
    }

    /**
     * Selects one page of the result. Pages start at 1.
     * A page that doesn't exist returns an empty list.
     *
     * @param page            the page number, starting at 1
     * @param entitiesPerPage the maximum number of entities per page
     * @return this object
     * @throws IllegalArgumentException if {@code page} or {@code entitiesPerPage} is less than 1
     */
    public QueryOptions page(int page, int entitiesPerPage) {
        if (page < 1 || entitiesPerPage < 1) {
            throw new IllegalArgumentException("page and entitiesPerPage must be at least 1");
        }
        this.limit = entitiesPerPage;
        // throws on int overflow
        this.skip = Math.multiplyExact(page - 1, entitiesPerPage);
        return this;
    }
}
