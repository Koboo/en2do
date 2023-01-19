package eu.koboo.en2do.repository.methods.pagination;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.HashMap;
import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Pagination {

    public static Pagination of(int entitiesPerPage) {
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

    public Pagination order(String fieldName, boolean ascending) {
        pageDirectionMap.put(fieldName, ascending ? 1 : -1);
        return this;
    }

    public Pagination order(String fieldName) {
        return order(fieldName, true);
    }

    public Pagination page(long page) {
        this.page = page;
        return this;
    }
}
