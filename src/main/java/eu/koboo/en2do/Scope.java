package eu.koboo.en2do;

import com.mongodb.client.model.Filters;
import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import org.bson.conversions.Bson;

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

    public String field(TypedPropertyGetter<T, ?> getter) {
        return PropertyUtils.getPropertyName(repository.getEntityClass(), getter);
    }
}