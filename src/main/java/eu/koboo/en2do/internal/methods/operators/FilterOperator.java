package eu.koboo.en2do.internal.methods.operators;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Represents a segment of the whole method filters.
 */
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum FilterOperator {

    /**
     * Represents Filters.eq(..)
     *
     * @see com.mongodb.client.model.Filters#eq(Object)
     */
    EQUALS("", 1),
    /**
     * Represents Filters.regex("(?i)^[value]$")
     *
     * @see com.mongodb.client.model.Filters#regex(String, String)
     */
    EQUALS_IGNORE_CASE("Ign", 1),
    /**
     * Represents Filters.gt(..)
     *
     * @see com.mongodb.client.model.Filters#gt(String, Object)
     */
    GREATER_THAN("GreaterThan", 1),
    /**
     * Represents Filters.lt(..)
     *
     * @see com.mongodb.client.model.Filters#lt(String, Object)
     */
    LESS_THAN("LessThan", 1),
    /**
     * Represents Filters.regex(..)
     *
     * @see com.mongodb.client.model.Filters#regex(String, String)
     * @see com.mongodb.client.model.Filters#regex(String, Pattern)
     */
    REGEX("Regex", 1),
    /**
     * Represents Filters.gte(..)
     *
     * @see com.mongodb.client.model.Filters#gte(String, Object)
     */
    GREATER_EQUALS("GreaterEq", 1),
    /**
     * Represents Filters.lte(..)
     *
     * @see com.mongodb.client.model.Filters#lte(String, Object)
     */
    LESS_EQUALS("LessEq", 1),
    /**
     * Represents Filters.exists(..)
     *
     * @see com.mongodb.client.model.Filters#exists(String)
     */
    EXISTS("Exists", 0),
    /**
     * Represents Filters.regex(".*[value].*")
     *
     * @see com.mongodb.client.model.Filters#regex(String, String)
     */
    CONTAINS("Contains", 1),
    /**
     * Represents Filters.gt(..) + Filters.lt(..)
     *
     * @see com.mongodb.client.model.Filters#gt(String, Object)
     * @see com.mongodb.client.model.Filters#lt(String, Object)
     */
    BETWEEN("Between", 2),
    /**
     * Represents Filters.gte(..) + Filters.lte(..)
     *
     * @see com.mongodb.client.model.Filters#gte(String, Object)
     * @see com.mongodb.client.model.Filters#lte(String, Object)
     */
    BETWEEN_EQUALS("BetweenEq", 2),
    /**
     * Represents Filters.in(..)
     *
     * @see com.mongodb.client.model.Filters#in(String, Object[])
     */
    IN("In", 1);

    public static final FilterOperator[] VALUES = FilterOperator.values();

    String keyword;
    int expectedParameterCount;

    /**
     * Removes the filter operator from end of the given text and returns it.
     *
     * @param textWithOperator The text, which has to end with the filter operator.
     * @return The text without the filter operator.
     */
    public @NotNull String removeOperatorFrom(@NotNull String textWithOperator) {
        return textWithOperator.substring(0, textWithOperator.length() - getKeyword().length());
    }

    /**
     * Parses a string to a filter operator by checking, if the text is ending with the
     * keyword of the filter operator, and returns the results.
     *
     * @param methodNamePart The text, which should be parsed.
     * @return The FilterOperator if any found, otherwise it fallbacks to EQUALS.
     */
    public static @NotNull FilterOperator parseFilterEndsWith(@NotNull String methodNamePart) {
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
