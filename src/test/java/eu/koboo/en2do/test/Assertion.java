package eu.koboo.en2do.test;

import junit.framework.AssertionFailedError;

import java.util.List;

public class Assertion {

    public static <T> void assertContains(List<T> list, T object) {
        if(!list.contains(object)) {
            throw new AssertionFailedError("List doesn't contains object");
        }
    }
}