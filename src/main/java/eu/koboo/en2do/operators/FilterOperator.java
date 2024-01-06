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
    HAS("Has", 1);

    public static final FilterOperator[] VALUES = FilterOperator.values();

    String keyword;
    int expectedParameterCount;

    /**
     * Removes the filter operator from end of the given text and returns it.
     *
     * @param textWithOperator The text, which has to end with the filter operator.
     * @return The text without the filter operator.
     */
    public String removeOperatorFrom(String textWithOperator) {
        return textWithOperator.substring(0, textWithOperator.length() - getKeyword().length());
    }

    /**
     * Parses a string to a filter operator by checking, if the text is ending with the
     * keyword of the filter operator, and returns the results.
     *
     * @param methodNamePart The text, which should be parsed.
     * @return The FilterOperator if any found, otherwise it fallbacks to EQUALS.
     */
    public static FilterOperator parseFilterEndsWith(String methodNamePart) {
        for (FilterOperator operator : VALUES) {
            if (operator == EQUALS) {
                continue;
            }
            if (!methodNamePart.endsWith(operator.getKeyword())) {
                continue;
            }
            return operator;
        }
        return EQUALS;
    }
}
