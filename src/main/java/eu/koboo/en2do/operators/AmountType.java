package eu.koboo.en2do.operators;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AmountType {

    FIRST(
        "First",
        false
    ),
    ONE(
        "One",
        false
    ),
    MANY(
        "Many",
        true
    ),
    ALL(
        "All",
        true
    ),
    TOP(
        "Top",
        true
    );

    public static final AmountType[] VALUES = AmountType.values();

    String keyword;
    boolean multipleEntities;

    public static long parseAmountByStringStartsWith(String string) {
        char[] charArray = string.toCharArray();
        StringBuilder numberBuilder = new StringBuilder();
        for (char stringChar : charArray) {
            if (Character.isDigit(stringChar)) {
                numberBuilder.append(stringChar);
                continue;
            }
            break;
        }
        String number = numberBuilder.toString();
        if (number.isEmpty()) {
            return 0;
        }
        return Long.parseLong(number);
    }
}
