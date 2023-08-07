package eu.koboo.en2do.utility;

import java.util.Arrays;
import java.util.List;

public class MethodUtils {

    public static final List<String> IGNORED_DEFAULT_METHODS = Arrays.asList(
        "notify", "notifyAll", "wait", "finalize", "clone"
    );
}
