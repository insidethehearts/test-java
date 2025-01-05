package me.therimuru.pulsebackend.httphandlers.me;

import com.sun.net.httpserver.HttpExchange;
import me.therimuru.pulsebackend.DatabaseManager;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;

import java.io.IOException;

public class GetProfileHttpHandler extends PulseHttpHandler {

    public GetProfileHttpHandler(DatabaseManager databaseManager) {
        super("me/profile", databaseManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
}
