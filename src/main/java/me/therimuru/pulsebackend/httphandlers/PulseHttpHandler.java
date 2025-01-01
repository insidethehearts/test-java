package me.therimuru.pulsebackend.httphandlers;

import com.sun.net.httpserver.HttpHandler;
import me.therimuru.pulsebackend.DatabaseManager;
import org.jetbrains.annotations.NotNull;

public abstract class PulseHttpHandler implements HttpHandler {

    private final @NotNull String feature;
    protected final @NotNull DatabaseManager databaseManager;

    public PulseHttpHandler(String feature, DatabaseManager databaseManager) {
        this.feature = feature;
        this.databaseManager = databaseManager;
    }

    public String getAbsoluteContext() {
        return "/api/" + feature;
    }
}