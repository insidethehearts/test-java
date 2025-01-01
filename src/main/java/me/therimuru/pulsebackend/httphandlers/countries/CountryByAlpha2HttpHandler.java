package me.therimuru.pulsebackend.httphandlers.countries;

import com.sun.net.httpserver.HttpExchange;
import me.therimuru.pulsebackend.DatabaseManager;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CountryByAlpha2HttpHandler extends PulseHttpHandler {

    public CountryByAlpha2HttpHandler(DatabaseManager databaseManager) {
        super("countries/", databaseManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement("SELECT name, alpha2, alpha3, region FROM countries WHERE alpha2 = ? ORDER BY alpha2;")) {
            String path = httpExchange.getRequestURI().getRawPath();
            statement.setString(1, path.substring(path.lastIndexOf("/") + 1));
            ResultSet resultSet = statement.executeQuery();
            JSONObject jsonObject = new JSONObject();
            while (resultSet.next()) {
                jsonObject.put("name", resultSet.getString("name"));
                jsonObject.put("alpha2", resultSet.getString("alpha2"));
                jsonObject.put("alpha3", resultSet.getString("alpha3"));
                jsonObject.put("region", resultSet.getString("region"));
            }

            byte[] response;
            
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            if (jsonObject.isEmpty()) {
                JSONObject responseJson = new JSONObject();
                responseJson.put("reason", "Invalid response code status");
                response = responseJson.toJSONString().getBytes(StandardCharsets.UTF_8);
                httpExchange.sendResponseHeaders(404, response.length);
            } else {
                response = jsonObject.toJSONString().getBytes();
                httpExchange.sendResponseHeaders(200, response.length);
            }

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response);

            outputStream.close();
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
