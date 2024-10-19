package me.goodbyepavlyi.lavarise.utils;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;

import java.util.logging.Level;

public class Logger {
    private static Logger instance;
    private final LavaRiseInstance lavaRiseInstance;
    private final boolean debug;

    public Logger(LavaRiseInstance lavaRiseInstance, boolean debug) {
        instance = this;
        this.lavaRiseInstance = lavaRiseInstance;
        this.debug = debug;
    }

    public static void log(Level level, String message) {
        if (instance == null)
            return;

        instance.lavaRiseInstance.getLogger().log(level, message);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    public static void severe(String message) {
        log(Level.SEVERE, message);
    }

    public static void debug(String message) {
        if (!instance.debug)
            return;

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTrace[3]; // Index 3 should point to the calling method

        instance.lavaRiseInstance.getLogger().log(Level.INFO, String.format("[DEBUG] (%s#%s) %s", caller.getClassName(), caller.getMethodName(), message));
    }
}
