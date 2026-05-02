package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection class for MAMP MySQL.
 * MAMP default: host=localhost, port=3306, user=root, password=root
 */
public class DBConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "ems_db2";
    private static final String USER     = "root";
    private static final String PASSWORD = "root"; // MAMP default

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static Connection instance = null;

    // Private constructor – no instantiation
    private DBConnection() {}

    /**
     * Returns a singleton Connection. Re-opens if closed.
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connection established.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j to classpath.", e);
            }
        }
        return instance;
    }

    /** Closes the connection (call on app exit). */
    public static void close() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
