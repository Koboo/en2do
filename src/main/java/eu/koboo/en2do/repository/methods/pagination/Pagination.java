package eu.koboo.en2do.repository.methods.pagination;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This object is used to provide simplified pagination in several repositories.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/usage/pagination">...</a>
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Pagination {

    /**
     * Use this method to create a new pagination object.
     *
     * @param entitiesPerPage Sets the maximum entities per one page.
     * @return The created Pagination object
     */
    public static @NotNull Pagination of(int entitiesPerPage) {
        return new Pagination(entitiesPerPage);
    }

    int entitiesPerPage;
    Map<String, Integer> pageDirectionMap;
    @NonFinal
    long page;

    private Pagination(int entitiesPerPage) {
        this.pageDirectionMap = new HashMap<>();
        this.entitiesPerPage = entitiesPerPage;
        this.page = 1;
    }

    /**
     * Use this method to define the order / sorting by the given fields
     *
     * @param fieldName The field which is used to sort
     * @param ascending The direction of the sorting
     * @return The used Pagination object
     */
    public @NotNull Pagination order(String fieldName, boolean ascending) {
        pageDirectionMap.put(fieldName, ascending ? 1 : -1);
        return this;
    }

    /**
     * Use this method to define the order / sorting of the pagination,
     * but sets the ascending value to "true"
     *
     * @param fieldName The field which is used to sort
     * @return The used Pagination object
     */
    public @NotNull Pagination order(String fieldName) {
        return order(fieldName, true);
    }

    /**
     * Use this method to set the returned page. If the page doesn't exist,
     * the method will just return an empty list.
     *
     * @param page The requested page
     * @return The used Pagination object
     */
    public @NotNull Pagination page(long page) {
        this.page = page;
        return this;
    }
}
