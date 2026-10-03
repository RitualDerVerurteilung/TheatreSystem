package util;

import util.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// * 1. Всего билетов
// * 2. Купленные (paid)
// * 3. Забронированные (booked)
// * 4. Отменённые (canceled)
// * 5. Количество спектаклей

public class Statistic {

    private final DatabaseManager databaseManager;

    public Statistic(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void printStatistics() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            // 1. Всего билетов
            int totalTickets = getCount(statement, "SELECT COUNT(*) FROM Ticket");
            System.out.println("Всего билетов: " + totalTickets);

            // 2. Купленные
            int paidTickets = getCount(statement, "SELECT COUNT(*) FROM Ticket WHERE status = 'paid'");
            System.out.println("Купленные билеты: " + paidTickets);

            // 3. Забронированные
            int bookedTickets = getCount(statement, "SELECT COUNT(*) FROM Ticket WHERE status = 'booked'");
            System.out.println("Забронированные билеты: " + bookedTickets);

            // 4. Отменённые
            int canceledTickets = getCount(statement, "SELECT COUNT(*) FROM Ticket WHERE status = 'canceled'");
            System.out.println("Отменённые билеты: " +canceledTickets);

            // 5. Количество спектаклей
            int totalPerformances = getCount(statement, "SELECT COUNT(*) FROM Performance");
            System.out.println("Количество спектаклей: " +totalPerformances);
        }
    }

    private int getCount(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }
}