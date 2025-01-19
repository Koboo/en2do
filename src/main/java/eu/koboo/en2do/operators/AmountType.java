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

    FIRST(false),
    ONE(false),
    MANY(true),
    ALL(true),
    TOP(true);

    public static final AmountType[] VALUES = AmountType.values();

    boolean multipleEntities;

    public String getOperatorString() {
        String typeName = name().toLowerCase(Locale.ROOT);
        return typeName.substring(0, 1).toUpperCase(Locale.ROOT) + typeName.substring(1);
    }

    public static AmountType parseTypeByStringStartsWith(String method) {
        for (AmountType type : VALUES) {
            String typeName = type.getOperatorString();
            if (!method.startsWith(typeName)) {
                continue;
            }
            return type;
        }
        return null;
    }

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
