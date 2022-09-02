package eu.koboo.en2do.utility;

import java.util.Arrays;
import java.util.List;

public class MethodNameUtil {

    private static final List<String> OPERATOR_NAMES = Arrays.asList(
            "findBy", "deleteBy"
    );

    public static String removeLeadingOperator(String methodName) {
        for (String operatorName : OPERATOR_NAMES) {
            if(!methodName.startsWith(operatorName)) {
                continue;
            }
            return methodName.replaceFirst(operatorName, "");
        }
        return null;
    }

}