package me.therimuru.pulsebackend.httphandlers.auth;

import com.sun.net.httpserver.HttpExchange;
import me.therimuru.pulsebackend.DatabaseManager;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterHttpHandler extends PulseHttpHandler {

    public RegisterHttpHandler(DatabaseManager databaseManager) {
        super("auth/register", databaseManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        User user;
        JSONObject userJson;
        InputStream requestBody = httpExchange.getRequestBody();

        try {
            userJson = asJsonObject(requestBody);
            user = new User(userJson);
            if (!user.isValid()) {
                responseError(httpExchange, 400, "Invalid data");
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            responseError(httpExchange, 400, "Invalid data");
            return;
        }

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement("SELECT login, email, phone FROM users WHERE login = ? OR email = ? OR phone = ?")) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                responseError(httpExchange, 409, "A user with such data already exists.");
                resultSet.close();
                return;
            }
            resultSet.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            serverError(httpExchange);
            return;
        }

        if (!user.isPasswordStrong()) {
            responseError(httpExchange, 400, "Password is not strong.");
            return;
        }

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement("SELECT * FROM countries WHERE alpha2 = ?")) {
            statement.setString(1, user.getCountryCode());
            ResultSet resultSet = statement.executeQuery();
            boolean validCountry = false;
            if (resultSet.next()) {
                validCountry = true;
            }
            if (!validCountry) {
                responseError(httpExchange, 400, "Country with that code not exist.");
                return;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            serverError(httpExchange);
            return;
        }

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement("INSERT INTO users (login, email, password, countryCode, public, phone, image) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            user.insertValues(statement);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            serverError(httpExchange);
            return;
        }

        String response = user.asProfileJsonObject().toJSONString();
        System.out.println(response);
        httpExchange.sendResponseHeaders(201, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
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

    private JSONObject asJsonObject(InputStream inputStream) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader((inputStream)));
        return jsonObject;
    }

    class User {

        private String login;
        private String email;
        private String password;
        private String countryCode;
        private String phone;
        private String image;
        private boolean isPublic;

        public User(JSONObject jsonObject) {
            login = (String) jsonObject.getOrDefault("login", null);
            email = (String) jsonObject.getOrDefault("email", null);
            password = (String) jsonObject.getOrDefault("password", null);
            countryCode = (String) jsonObject.getOrDefault("countryCode", null);
            phone = (String) jsonObject.getOrDefault("phone", null);
            image = (String) jsonObject.getOrDefault("image", null);
            isPublic = (boolean) jsonObject.getOrDefault("isPublic", null);
        }

        public JSONObject asProfileJsonObject() {
            JSONObject jsonProfile = new JSONObject();
            jsonProfile.put("login", String.valueOf(login));
            jsonProfile.put("email", String.valueOf(email));
            jsonProfile.put("countryCode", String.valueOf(countryCode));
            jsonProfile.put("isPublic", Boolean.valueOf(isPublic));
            if (phone != null)
                jsonProfile.put("phone", phone);
            if (image != null)
                jsonProfile.put("image", image);

            JSONObject response = new JSONObject();
            response.put("profile", jsonProfile);
            return response;
        }

        public boolean isPasswordStrong() {
            return !(password.length() < 6 || !password.chars().anyMatch(Character::isDigit) ||
                    password.toLowerCase().equals(password) || password.toUpperCase().equals(password));
        }

        public String getLogin() {
            return login;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getPhone() {
            return phone;
        }

        public String getImage() {
            return image;
        }

        public boolean isPublic() {
            return isPublic;
        }

        public boolean isValid() {
            return login != null && email != null && password != null && countryCode != null;
        }

        public void insertValues(PreparedStatement statement) throws SQLException {
            statement.setString(1, login);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, countryCode);
            statement.setBoolean(5, isPublic);

            if (phone != null) statement.setString(6, phone);
            else statement.setString(6, "NULL");

            if (image != null) statement.setString(7, image);
            else statement.setString(7, "NULL");
        }

        @Override
        public String toString() {
            return "User{" +
                    "login='" + login + '\'' +
                    ", email='" + email + '\'' +
                    ", password='" + password + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    ", phone='" + phone + '\'' +
                    ", image='" + image + '\'' +
                    ", isPublic=" + isPublic +
                    ", valid=" + isValid() + '\'' +
                    '}';
        }
    }

}