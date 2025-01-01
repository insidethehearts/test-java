package me.therimuru.pulsebackend.httphandlers.countries;

import com.sun.net.httpserver.HttpExchange;
import me.therimuru.pulsebackend.DatabaseManager;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CountriesHttpHandler extends PulseHttpHandler {

    public CountriesHttpHandler(DatabaseManager databaseManager) {
        super("countries" , databaseManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement("SELECT name, alpha2, alpha3, region FROM countries ORDER BY alpha2;")) {
            ResultSet resultSet = statement.executeQuery();
            JSONArray jsonCountries = new JSONArray();

            while (resultSet.next()) {
                JSONObject country = new JSONObject();
                country.put("name", resultSet.getString("name"));
                country.put("alpha2", resultSet.getString("alpha2"));
                country.put("alpha3", resultSet.getString("alpha3"));
                country.put("region", resultSet.getString("region"));
                jsonCountries.add(country);
            }

            byte[] response = jsonCountries.toJSONString().getBytes();

            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.length);

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response);

            outputStream.close();
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
