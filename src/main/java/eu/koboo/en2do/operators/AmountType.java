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

    public String getOperatorName() {
        String typeName = name().toLowerCase(Locale.ROOT);
        return typeName.substring(0, 1).toUpperCase(Locale.ROOT) + typeName.substring(1);
    }

    public static AmountType parseTypeByStringStartsWith(String method) {
        for (AmountType type : VALUES) {
            String typeName = type.getOperatorName();
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

    public long parseEntityAmount(String methodName) {
        switch (this) {
            case ONE:
            case FIRST:
                return 1;
            case TOP:
                long entityAmount = AmountType.parseAmountByStringStartsWith(methodName);
                if (entityAmount == 0) {
                    throw new RuntimeException("The entityAmount 0 is not a valid top number.");
                }
                return entityAmount;
            case MANY:
            case ALL:
                // Doesn't get used anyway.
                return -1;
            default:
                throw new IllegalArgumentException("Cannot parse entity amount by type " + name() + " " + methodName);
        }
    }
}
