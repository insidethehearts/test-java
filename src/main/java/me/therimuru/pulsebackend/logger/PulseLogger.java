package me.therimuru.pulsebackend.logger;

/**
 * <p>A simple class for logging colored information to console.</p>
 * <p>You can toggle debugging mode if you need it with {@link #debugMode(boolean)}</p>
 * <p>Use {@link #log(LogType, String)} voids to log.</p>
 * @author therimuru_
 */

public class PulseLogger {

    private PulseLogger() {}

    /**
     * @param debugMode enable/disable debug mode
     */
    public static void debugMode(boolean debugMode) {
        debug = debugMode;
    }

    /**
     * By default, debug mode is enabled.
     */
    private static boolean debug = true;

    /**
     *
     * @param logType Type of information to log
     * @param text - Information to log
     */
    public static void log(LogType logType, String text) {
        if (!debug && logType == LogType.DEBUG) return;
        System.out.println(logType.color() + "[" + logType + "] " + text + "\u001B[0m");
    }
}

