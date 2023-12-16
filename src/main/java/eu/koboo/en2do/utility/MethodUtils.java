package eu.koboo.en2do.utility;

import java.util.Arrays;
import java.util.List;

public class MethodUtils {

    /**
     * These methods are ignored from en2do validation, because they are default java
     * methods, which doesn't get implement by en2do repositories.
     */
    public static final List<String> IGNORED_DEFAULT_METHODS = Arrays.asList(
        "notify", "notifyAll", "wait", "finalize", "clone"
    );
}
