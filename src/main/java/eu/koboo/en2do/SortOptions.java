package eu.koboo.en2do;

import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SortOptions<T> {

    TypedPropertyGetter<T, ?> field;
    boolean sortAscending = false;
    int limit = -1;

    public SortOptions(TypedPropertyGetter<T, ?> field, boolean sortAscending, int limit) {
        this.field = field;
        this.sortAscending = sortAscending;
        this.limit = limit;
    }

    public SortOptions(TypedPropertyGetter<T, ?> field, boolean sortAscending) {
        this.field = field;
        this.sortAscending = sortAscending;
    }

    public SortOptions(TypedPropertyGetter<T, ?> field, int limit) {
        this.field = field;
        this.limit = limit;
    }

    public SortOptions(TypedPropertyGetter<T, ?> field) {
        this.field = field;
    }

    public SortOptions(int limit) {
        this.limit = limit;
    }
}