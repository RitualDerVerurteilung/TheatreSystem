package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // final - после присваивания значение поля нельзя изменить
    private final String url;
    private final String username;
    private final String password;

    // Конструктор
    public DatabaseManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // Метод, который создаёт и возвращает соединение (объект Connection) с БД
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
                url,
                username,
                password
        );

        // Проверка соединения, число 2 — макс. время проверки в секундах
        if (!connection.isValid(2)) {
            connection.close();
            throw new SQLException("Не удалось установить соединение с базой данных");
        }

        return connection;
    }

    // Проверка, доступна ли база данных для подключения
    public boolean isConnected() {
        // Конструкция try-with-resources для автоматического закрытия
        try (Connection connection = DriverManager.getConnection(
                url,
                username,
                password
        )) {
            // Проверка соединения
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}

// ======== ПОДКЛЮЧЕНИЕ К БД ========
// import util.DatabaseManager;
//
// DatabaseManager databaseManager = new DatabaseManager(
//     "jdbc:postgresql://localhost:5432/Theatre",
//     "postgres",
//     "password"
// );
//
// System.out.println( "Подключение: " + databaseManager.isConnected() );