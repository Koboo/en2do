package eu.koboo.en2do;

import lombok.extern.java.Log;

@Log
public class Debugger {

    private static final boolean printDebugMessages = false;

    public static void print(String message) {
        if (!printDebugMessages) {
            return;
        }
        log.finest("[DEBUGGER] " + message);
    }
}
