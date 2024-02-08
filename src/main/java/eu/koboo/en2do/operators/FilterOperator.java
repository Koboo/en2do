package eu.koboo.en2do.operators;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Represents a segment of the whole method filters.
 */
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum FilterOperator {

    EQUALS("", 1),
    EQUALS_IGNORE_CASE("Ign", 1),
    GREATER_THAN("GreaterThan", 1),
    LESS_THAN("LessThan", 1),
    REGEX("Regex", 1),
    GREATER_EQUALS("GreaterEq", 1),
    LESS_EQUALS("LessEq", 1),
    EXISTS("Exists", 0),
    CONTAINS("Contains", 1),
    BETWEEN("Between", 2),
    BETWEEN_EQUALS("BetweenEq", 2),
    IN("In", 1),
    HAS_KEY("HasKey", 1),
    HAS("Has", 1),
    GEO("Geo", 1),

    IS_NULL("IsNull", 0),
    NON_NULL("NonNull", 0),
    IS_TRUE("IsTrue", 0),
    IS_FALSE("IsFalse", 0),

    ;

    public static final FilterOperator[] VALUES = FilterOperator.values();

    String keyword;
    int expectedParameterCount;
}