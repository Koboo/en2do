package eu.koboo.en2do.utility;

import java.util.Arrays;
import java.util.List;

public class MethodNameUtil {

    private static final List<String> OPERATOR_NAMES = Arrays.asList(
            "findBy", "deleteBy"
    );

    private static final List<String> FILTER_NAMES = Arrays.asList(
            "Equals", "EqualsIgnoreCase", "GreaterThan", "LessThan", "Has",
            "Regex", "GreaterEquals", "LessEquals"
    );

    public static boolean containsAnyFilter(String methodName) {
        for (String filterName : FILTER_NAMES) {
            if(!methodName.contains(filterName)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public static String removeLeadingOperator(String methodName) {
        for (String operatorName : OPERATOR_NAMES) {
            if(!methodName.startsWith(operatorName)) {
                continue;
            }
            return methodName.replaceFirst(operatorName, "");
        }
        return null;
    }

    public static String replaceEndingFilter(String operator) {
        for (String filterName : FILTER_NAMES) {
            if(!operator.endsWith(filterName)) {
                continue;
            }
            return operator.substring(0, operator.length() - filterName.length());
        }
        return null;
    }
}