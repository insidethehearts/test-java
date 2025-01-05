package me.therimuru.pulsebackend.httphandlers.auth;

import com.auth0.jwt.JWT;
import com.sun.net.httpserver.HttpExchange;
import me.therimuru.pulsebackend.DatabaseManager;
import me.therimuru.pulsebackend.Pulse;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Date;

public class SignInHttpHandler extends PulseHttpHandler {

    public SignInHttpHandler(DatabaseManager databaseManager) {
        super("auth/sign-in", databaseManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonUserData;

        try {
            jsonUserData = (JSONObject) jsonParser.parse(new InputStreamReader(httpExchange.getRequestBody()));
        } catch (ParseException e) {
            e.printStackTrace();
            responseError(httpExchange, 400, "Invalid data");
            return;
        }

        UserData userData = new UserData(jsonUserData);
        if (!userData.isValid()) {
            responseError(httpExchange, 400, "Invalid user data");
            return;
        }

        boolean valid = false;
        String login = null;
        String phone = null;
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement("SELECT * FROM users WHERE login = ? AND password = ? LIMIT 1")) {
            statement.setString(1, userData.login);
            statement.setString(2, userData.password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                valid = true;
                login = resultSet.getString("login");
                phone = resultSet.getString("phone");
            }
        } catch (SQLException e) {
            serverError(httpExchange);
            return;
        }

        if (!valid) {
            responseError(httpExchange, 401, "User not found");
        } else {
            String token = JWT.create()
                    .withExpiresAt(Date.from(new Date().toInstant().plus(Duration.ofHours(1))))
                    .withClaim("login", login)
                    .withClaim("phone", phone)
                    .sign(Pulse.getAlgorithm());

            JSONObject responseJson = new JSONObject();
            responseJson.put("token", token);

            String response = responseJson.toJSONString();
            httpExchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        }
    }

    private void responseError(HttpExchange httpExchange, int code, String reason) throws IOException {
        JSONObject responseJson = new JSONObject();
        responseJson.put("reason", reason);
        String response = responseJson.toJSONString();
        httpExchange.sendResponseHeaders(code, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
        httpExchange.close();
    }

    private void serverError(HttpExchange httpExchange) throws IOException {
        responseError(httpExchange, 500, "An unexpected error occurred on the server.");
    }

    class UserData {

        private final String login;
        private final String password;

        public UserData(JSONObject jsonObject) {
            login = (String) jsonObject.getOrDefault("login", null);
            password = (String) jsonObject.getOrDefault("password", null);
        }

        public boolean isValid() {
            return login != null && password != null;
        }
    }
}
