package me.therimuru.pulsebackend;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import me.therimuru.pulsebackend.httphandlers.PulseHttpHandler;
import me.therimuru.pulsebackend.httphandlers.auth.RegisterHttpHandler;
import me.therimuru.pulsebackend.httphandlers.auth.SignInHttpHandler;
import me.therimuru.pulsebackend.httphandlers.countries.CountriesHttpHandler;
import me.therimuru.pulsebackend.httphandlers.countries.CountryByAlpha2HttpHandler;
import me.therimuru.pulsebackend.httphandlers.me.GetProfileHttpHandler;
import me.therimuru.pulsebackend.logger.LogType;
import me.therimuru.pulsebackend.logger.PulseLogger;
import me.therimuru.pulsebackend.httphandlers.ping.PingHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author therimuru_
 */

public class Pulse {

    private static String JWT_SECRET = "_SECRET_HERE_";
    private static JWTVerifier jwtVerifier;
    private static Algorithm algorithm;

    private static final String POSTGRES_USER = "therimuru";
    private static final String POSTGRES_PASSWORD = "therimuru";
    private static final String POSTGRES_JDBC_URL = "jdbc:postgresql://127.0.0.1/test";

    private static int HTTPSERVER_PORT;

    private static HttpServer httpServer;
    private static DatabaseManager databaseManager;

    private static List<PulseHttpHandler> httpHandlers = new ArrayList<>();

    public static void main(String[] args) {

        PulseLogger.debugMode(true);

        HTTPSERVER_PORT = 8080;

        algorithm = Algorithm.HMAC256(JWT_SECRET);
        jwtVerifier = JWT.require(algorithm)
                .withIssuer(JWT_SECRET)
                .build();

        try {
            initDB();
            PulseLogger.log(LogType.FINE, "Successfully initialized: PostgresSQL connection");
        } catch (SQLException e) {
            PulseLogger.log(LogType.ERROR, "An error occurred while attempting to initialize a connection to the database. Information has been output to the console.");
            e.printStackTrace();
            exit(1);
        } catch (ClassNotFoundException e) {
            PulseLogger.log(LogType.ERROR, "An error occurred while attempting to initialize a connection to the database. Information has been output to the console.");
            e.printStackTrace();
            exit(2);
        }

        preloadHandlers();

        try {
            initHttpServer(HTTPSERVER_PORT);
            PulseLogger.log(LogType.FINE, "Successfully initialized: HttpServer");
        } catch (IOException e) {
            PulseLogger.log(LogType.ERROR, "An error occurred while attempting to initialize the HTTP server. Information is displayed in the console.");
            e.printStackTrace();
            exit(3);
        }
    }

    private static void initDB() throws SQLException, ClassNotFoundException {
        databaseManager = new DatabaseManager(POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_JDBC_URL);
    }

    private static void preloadHandlers() {
        httpHandlers.add(new PingHttpHandler(databaseManager));

        httpHandlers.add(new CountriesHttpHandler(databaseManager));
        httpHandlers.add(new CountryByAlpha2HttpHandler(databaseManager));

        httpHandlers.add(new RegisterHttpHandler(databaseManager));
        httpHandlers.add(new SignInHttpHandler(databaseManager));

        httpHandlers.add(new GetProfileHttpHandler(databaseManager));
    }

    private static void initHttpServer(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port),0);
        httpServer.setExecutor(null);

        httpHandlers.forEach(pulseHttpHandler -> httpServer.createContext(pulseHttpHandler.getAbsoluteContext(), pulseHttpHandler));

        httpServer.start();

        PulseLogger.log(LogType.DEBUG, "Listening port: " + port);
    }

    public static Algorithm getAlgorithm() {
        return algorithm;
    }

    public static JWTVerifier getVerifier() {
        return jwtVerifier;
    }

    public static void exit(int code) {
        try {
            databaseManager.disconnect();
            httpServer.stop(0);
        } catch (SQLException ignored) {}
        System.exit(code);
    }
}