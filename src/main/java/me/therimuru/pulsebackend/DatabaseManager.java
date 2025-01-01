package me.therimuru.pulsebackend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private Connection connection;

    public DatabaseManager(String POSTGRES_USER, String POSTGRES_PASSWORD, String POSTGRES_JDBC_URL) throws SQLException, ClassNotFoundException {
        connect(POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_JDBC_URL);
        initializeTables();
    }

    private void connect(String POSTGRES_USER, String POSTGRES_PASSWORD, String POSTGRES_JDBC_URL) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(POSTGRES_JDBC_URL, POSTGRES_USER, POSTGRES_PASSWORD);
    }

    private void initializeTables() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS users (
                  uid SERIAL PRIMARY KEY,
                  login VARCHAR(255) NOT NULL,
                  email VARCHAR(255) NOT NULL,
                  password VARCHAR(255) NOT NULL,
                  countryCode VARCHAR(2) NOT NULL,
                  phone VARCHAR(255) NULL,
                  image VARCHAR(255) NULL,
                  public BOOLEAN NOT NULL
                )
                """)) {
            statement.executeUpdate();
        }
    }

    /*
        private String login;
        private String email;
        private String password;
        private String countryCode;
        private String phone;
        private String image;
        private boolean isPublic;
     */

    public void disconnect() throws SQLException {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }
}