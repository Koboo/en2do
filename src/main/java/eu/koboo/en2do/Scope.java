package eu.koboo.en2do;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Scope<T, ID> {

    private final Repository<T, ID> repository;

    protected Scope(Repository<T, ID> repository) {
        this.repository = repository;
    }

    public Bson and(Bson... filters) {
        return Filters.and(filters);
    }

    public Bson or(Bson... filters) {
        return Filters.or(filters);
    }

    public Bson nor(Bson... filters) {
        return Filters.nor(filters);
    }

    public <V> Bson regex(TypedPropertyGetter<T, V> getter, String pattern) {
        return Filters.regex(field(getter), pattern);
    }

    public <V> Bson regex(TypedPropertyGetter<T, V> getter, Pattern pattern) {
        return Filters.regex(field(getter), pattern);
    }

    public <V> Bson eq(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.eq(field(getter), value);
    }

    public <V> Bson notEq(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.not(eq(getter, value));
    }

    public <V> Bson eqIgn(TypedPropertyGetter<T, V> getter, V value) {
        String patternString = "(?i)^" + value + "$";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        return Filters.regex(field(getter), pattern);
    }

    public <V> Bson notEqIgn(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.not(eqIgn(getter, value));
    }

    public <V> Bson has(TypedPropertyGetter<T, V> getter) {
        return Filters.exists(field(getter));
    }

    public <V> Bson hasNot(TypedPropertyGetter<T, V> getter) {
        return Filters.not(Filters.exists(field(getter)));
    }

    // Greater Than
    public <V> Bson gt(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.gt(field(getter), value);
    }

    // Not Greater Than
    public <V> Bson notGt(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.not(Filters.gt(field(getter), value));
    }

    // Greater Than Equals
    public <V> Bson gte(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.gte(field(getter), value);
    }

    // Not Greater Than Equals
    public <V> Bson notGte(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.not(Filters.gte(field(getter), value));
    }

    // Lower Than
    public <V> Bson lt(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.lt(field(getter), value);
    }

    // Not Lower Than
    public <V> Bson notLt(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.not(Filters.lt(field(getter), value));
    }

    // Not Lower Than Equals
    public <V> Bson lte(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.lte(field(getter), value);
    }

    // Not Lower Than Equals
    public <V> Bson notLte(TypedPropertyGetter<T, V> getter, V value) {
        return Filters.not(Filters.lte(field(getter), value));
    }

    public <V> Bson in(TypedPropertyGetter<T, V> getter, Iterable<V> values) {
        return Filters.in(field(getter), values);
    }

    public <V> Bson in(TypedPropertyGetter<T, V> getter, V... values) {
        return in(getter, Arrays.asList(values));
    }

    public <V> Bson notIn(TypedPropertyGetter<T, V> getter, List<V> values) {
        return Filters.nin(field(getter), values);
    }

    public <V> Bson notIn(TypedPropertyGetter<T, V> getter, V... values) {
        return notIn(getter, Arrays.asList(values));
    }

    public <V> Bson sort(TypedPropertyGetter<T, V> getter, boolean ascending) {
        return new BasicDBObject(field(getter), ascending ? 1 : -1);
    }

    public <V> Bson sortAsc(TypedPropertyGetter<T, V> getter) {
        return sort(getter, true);
    }

    public <V> Bson sortDesc(TypedPropertyGetter<T, V> getter) {
        return sort(getter, false);
    }

    private String field(TypedPropertyGetter<T, ?> getter) {
        return PropertyUtils.getPropertyName(repository.getEntityClass(), getter);
    }
}