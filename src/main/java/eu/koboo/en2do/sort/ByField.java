package eu.koboo.en2do.sort;

public record ByField(String fieldName, boolean ascending) {

    public static ByField of(String field, boolean ascending) {
        return new ByField(field, ascending);
    }

    public static ByField of(String field) {
        return new ByField(field, false);
    }

}