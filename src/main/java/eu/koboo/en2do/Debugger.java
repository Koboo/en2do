package eu.koboo.en2do;

public class Debugger {

    private static final boolean printDebugMessages = false;

    public static void print(String message) {
        if(!printDebugMessages) {
            return;
        }
        System.out.print("[DEBUGGER] " + message);
    }
}