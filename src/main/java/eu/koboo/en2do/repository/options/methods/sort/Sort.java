package eu.koboo.en2do.repository.options.methods.sort;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sort {

    @Deprecated
    public static Sort create() {
        return of();
    }

    public static Sort of() {
        return new Sort();
    }

    final Map<String, Integer> fieldDirectionMap;
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
