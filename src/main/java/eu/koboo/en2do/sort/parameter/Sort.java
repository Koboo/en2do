package eu.koboo.en2do.sort.parameter;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Sort {

    public static Sort create() {
        return new Sort();
    }

    List<ByField> byFieldList;
    int limit;
    int skip;

    private Sort() {
        this.byFieldList = new ArrayList<>();
        this.limit = -1;
        this.skip = -1;
    }

    public Sort order(ByField byField) {
        byFieldList.add(byField);
        return this;
    }

    public Sort order(String fieldName, boolean ascending) {
        return order(ByField.of(fieldName, ascending));
    }

    public Sort order(String fieldName) {
        return order(ByField.of(fieldName));
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
