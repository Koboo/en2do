package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class MethodUtils {

    /**
     * These methods are ignored from en2do validation, because they are default java
     * methods, which doesn't get implement by en2do repositories.
     */
    public static final List<String> IGNORED_DEFAULT_METHODS = Arrays.asList(
        "notify", "notifyAll", "wait", "finalize", "clone"
    );

    public long getPrefixedNumber(String string) {
        char[] charArray = string.toCharArray();
        StringBuilder numberBuilder = new StringBuilder();
        for (char c : charArray) {
            if (Character.isDigit(c)) {
                numberBuilder.append(c);
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
