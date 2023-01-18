package eu.koboo.en2do.repository.methods.paging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.HashMap;
import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Pager {

    public static Pager of(int entitiesPerPage) {
        return new Pager(entitiesPerPage);
    }

    int entitiesPerPage;
    Map<String, Integer> pageDirectionMap;
    @NonFinal
    long page;

    private Pager(int entitiesPerPage) {
        this.pageDirectionMap = new HashMap<>();
        this.entitiesPerPage = entitiesPerPage;
        this.page = 1;
    }

    public Pager order(String fieldName, boolean ascending) {
        pageDirectionMap.put(fieldName, ascending ? 1 : -1);
        return this;
    }

    public Pager order(String fieldName) {
        return order(fieldName, true);
    }

    public Pager page(long page) {
        this.page = page;
        return this;
    }
}
