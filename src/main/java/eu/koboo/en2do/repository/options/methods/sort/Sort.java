package eu.koboo.en2do.repository.options.methods.sort;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Sort {

    public static Sort create() {
        return new Sort();
    }

    Map<String, Integer> fieldDirectionMap;
    int limit;
    int skip;

    private Sort() {
        this.fieldDirectionMap = new HashMap<>();
        this.limit = -1;
        this.skip = -1;
    }

    public Sort order(String fieldName, boolean ascending) {
        fieldDirectionMap.put(fieldName, ascending ? 1 : -1);
        return this;
    }

    public Sort order(String fieldName) {
        return order(fieldName, true);
    }

    public Sort limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Sort skip(int skip) {
        this.skip = skip;
        return this;
    }
}
