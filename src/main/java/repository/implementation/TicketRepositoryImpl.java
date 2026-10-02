package repository.implementation;

import util.DatabaseManager;
import model.Ticket;
import model.Ticket.TicketStatus;
import repository.TicketRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketRepositoryImpl implements TicketRepository {

    private final DatabaseManager databaseManager;

    public TicketRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public List<Ticket> findByTicketStatus(
            int userId,
            TicketStatus status
    ) throws SQLException {

        String sql = """
            SELECT id,
                   user_id,
                   performance_id,
                   row_number,
                   seat_number,
                   status,
                   created_at
            FROM Ticket
            WHERE user_id = ?
              AND status = ?::ticket_status
            ORDER BY created_at DESC
            """;

        List<Ticket> tickets = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, statusToDatabase(status));

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    tickets.add(mapTicket(resultSet));
                }
            }
        }

        return tickets;
    }

    @Override
    public List<Ticket> findByPerformanceStatus(
            int userId,
            String performanceStatus
    ) throws SQLException {

        String sql;

        if ("upcoming".equalsIgnoreCase(performanceStatus)) {

            sql = """
                SELECT t.id,
                       t.user_id,
                       t.performance_id,
                       t.row_number,
                       t.seat_number,
                       t.status,
                       t.created_at
                FROM Ticket t
                JOIN Performance p
                    ON p.id = t.performance_id
                WHERE t.user_id = ?
                  AND CURRENT_TIMESTAMP <
                      p.performance_date +
                      (p.duration * INTERVAL '1 minute')
                ORDER BY p.performance_date DESC
                """;

        } else if ("finished".equalsIgnoreCase(performanceStatus)) {

            sql = """
                SELECT t.id,
                       t.user_id,
                       t.performance_id,
                       t.row_number,
                       t.seat_number,
                       t.status,
                       t.created_at
                FROM Ticket t
                JOIN Performance p
                    ON p.id = t.performance_id
                WHERE t.user_id = ?
                  AND CURRENT_TIMESTAMP >=
                      p.performance_date +
                      (p.duration * INTERVAL '1 minute')
                ORDER BY p.performance_date DESC
                """;

        } else {
            throw new IllegalArgumentException(
                    "Неизвестный статус спектакля: " + performanceStatus
            );
        }

        return executeTicketListQuery(sql, userId);
    }

    @Override
    public List<Ticket> findAllSortedByStatus(int userId)
            throws SQLException {

        String sql = """
            SELECT id,
                   user_id,
                   performance_id,
                   row_number,
                   seat_number,
                   status,
                   created_at
            FROM Ticket
            WHERE user_id = ?
            ORDER BY
                CASE status
                    WHEN 'paid' THEN 1
                    WHEN 'booked' THEN 2
                    WHEN 'canceled' THEN 3
                END,
                created_at DESC
            """;

        return executeTicketListQuery(sql, userId);
    }

    @Override
    public List<Ticket> findAllSortedByDate(int userId)
            throws SQLException {

        String sql = """
            SELECT t.id,
                   t.user_id,
                   t.performance_id,
                   t.row_number,
                   t.seat_number,
                   t.status,
                   t.created_at
            FROM Ticket t
            JOIN Performance p
                ON p.id = t.performance_id
            WHERE t.user_id = ?
            ORDER BY p.performance_date DESC
            """;

        return executeTicketListQuery(sql, userId);
    }

    @Override
    public Optional<Ticket> findById(
            int id,
            int userId
    ) throws SQLException {

        String sql = """
            SELECT id,
                   user_id,
                   performance_id,
                   row_number,
                   seat_number,
                   status,
                   created_at
            FROM Ticket
            WHERE id = ?
              AND user_id = ?
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapTicket(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean updateStatus(
            int id,
            int userId,
            TicketStatus newStatus
    ) throws SQLException {

        String sql = """
            UPDATE Ticket
            SET status = ?::ticket_status
            WHERE id = ?
              AND user_id = ?
              AND (
                    (status = 'booked' AND ? IN ('paid', 'canceled'))
                    OR
                    (status = 'paid' AND ? = 'canceled')
                  )
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String status = statusToDatabase(newStatus);

            statement.setString(1, status);
            statement.setInt(2, id);
            statement.setInt(3, userId);
            statement.setString(4, status);
            statement.setString(5, status);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(
            int id,
            int userId
    ) throws SQLException {

        String sql = """
            DELETE FROM Ticket
            WHERE id = ?
              AND user_id = ?
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public Ticket create(
            int userId,
            int performanceId,
            int rowNumber,
            int seatNumber
    ) throws SQLException {

        String sql = """
            INSERT INTO Ticket (
                user_id,
                performance_id,
                row_number,
                seat_number,
                status
            )
            VALUES (?, ?, ?, ?, 'booked')
            RETURNING id,
                      user_id,
                      performance_id,
                      row_number,
                      seat_number,
                      status,
                      created_at
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, performanceId);
            statement.setInt(3, rowNumber);
            statement.setInt(4, seatNumber);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapTicket(resultSet);
                }
            }

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new SQLException(
                        "Это место уже занято",
                        e
                );
            }

            throw e;
        }

        throw new SQLException("Не удалось создать билет");
    }

    @Override
    public int[][] getSeats(int performanceId)
            throws SQLException {

        String sql = """
            SELECT row_number,
                   seat_number
            FROM Ticket
            WHERE performance_id = ?
              AND status IN ('booked', 'paid')
            ORDER BY row_number, seat_number
            """;

        int[][] seats = new int[15][20];

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, performanceId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    int row =
                            resultSet.getInt("row_number");

                    int seat =
                            resultSet.getInt("seat_number");

                    seats[row - 1][seat - 1] = 1;
                }
            }
        }

        return seats;
    }

    private List<Ticket> executeTicketListQuery(
            String sql,
            int userId
    ) throws SQLException {

        List<Ticket> tickets = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    tickets.add(mapTicket(resultSet));
                }
            }
        }

        return tickets;
    }

    private Ticket mapTicket(ResultSet resultSet)
            throws SQLException {

        Ticket ticket = new Ticket();

        ticket.setId(resultSet.getInt("id"));
        ticket.setUserId(resultSet.getInt("user_id"));
        ticket.setPerformanceId(
                resultSet.getInt("performance_id")
        );
        ticket.setRowNumber(
                resultSet.getInt("row_number")
        );
        ticket.setSeatNumber(
                resultSet.getInt("seat_number")
        );

        ticket.setStatus(
                TicketStatus.valueOf(
                        resultSet
                                .getString("status")
                                .toUpperCase()
                )
        );

        ticket.setCreatedAt(
                resultSet.getString("created_at")
        );

        return ticket;
    }

    private String statusToDatabase(TicketStatus status) {

        return switch (status) {
            case BOOKED -> "booked";
            case PAID -> "paid";
            case CANCELED -> "canceled";
        };
    }

}
