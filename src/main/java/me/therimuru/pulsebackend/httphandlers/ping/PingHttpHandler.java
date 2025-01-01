package me.therimuru.pulsebackend.httphandlers.ping;

import me.therimuru.pulsebackend.DatabaseManager;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class PingHttpHandler extends PulseHttpHandler {

    public PingHttpHandler(DatabaseManager databaseManager) {
        super("ping", databaseManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        byte[] response = "The server is ready to accept requests.".getBytes();
        httpExchange.sendResponseHeaders(200, response.length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response);
        outputStream.close();
    }
}
