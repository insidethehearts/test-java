package me.therimuru.pulsebackend.logger;

 /**
 * @author therimuru_
 */

public enum LogType {
    FINE("\u001B[32m"),
    DEBUG("\u001B[34m"),
    ERROR("\u001B[31m"),
    INFO("\u001B[0m");

    private final String colorCode;

    LogType(String colorCode) {
        this.colorCode = colorCode;
    }

    /**
     * @return ANSI color as {@link String}
     */
    public String color() {
        return colorCode;
    }
}