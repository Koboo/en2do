package eu.koboo.en2do.repository;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

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
    IN("In", 1);

    public static final FilterOperator[] VALUES = FilterOperator.values();

    String keyword;
    int expectedParameterCount;

    public String removeOperatorFrom(String textWithOperator) {
        return textWithOperator.substring(0, textWithOperator.length() - getKeyword().length());
    }

    public static FilterOperator parseFilterEndsWith(String methodNamePart) {
        for (FilterOperator operator : VALUES) {
            if(operator == EQUALS) {
                continue;
            }
            if(!methodNamePart.endsWith(operator.getKeyword())) {
                continue;
            }
            return operator;
        }
        return EQUALS;
    }
}