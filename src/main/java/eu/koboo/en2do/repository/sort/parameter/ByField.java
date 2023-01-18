package eu.koboo.en2do.repository.sort.parameter;

public record ByField(String fieldName, boolean ascending) {

    public static ByField of(String field, boolean ascending) {
        return new ByField(field, ascending);
    }

    public static ByField of(String field) {
        return new ByField(field, false);
    }
}
