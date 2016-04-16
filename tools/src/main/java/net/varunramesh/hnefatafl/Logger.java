package net.varunramesh.hnefatafl;

/**
 * Created by varunramesh on 4/16/16.
 */
public class Logger {
    public static void debug(String tag, String message) {
        System.out.println("[" + tag + "] " + message);
    }
    public static void wtf(String tag, String message) {
        System.out.println("[" + tag + "] " + message);
    }
}
