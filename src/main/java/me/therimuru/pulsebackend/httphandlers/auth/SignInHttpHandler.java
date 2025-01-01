package me.therimuru.pulsebackend.httphandlers.auth;

import com.sun.net.httpserver.HttpExchange;
import me.therimuru.pulsebackend.DatabaseManager;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;

import java.io.IOException;

public class SignInHttpHandler extends PulseHttpHandler {

    public SignInHttpHandler(DatabaseManager databaseManager) {
        super("auth/sign-in", databaseManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
}
