package repository.implementation;

import util.DatabaseManager;
import model.Ticket;
import model.Ticket.TicketStatus;
import repository.TicketRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// implements — класс TicketRepositoryImpl реализует интерфейс TicketRepository
public class TicketRepositoryImpl implements TicketRepository {

    // final — после присваивания значения поле нельзя заменить
    // т.е. один раз передаётся объект подключения
    private final DatabaseManager databaseManager;

    // Конструктор
    public TicketRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    @Override
    public List<Ticket> findByTicketStatus(
            int userId,
            TicketStatus status
    ) throws SQLException {

        // ? — плейсхолдер
        // ?:: — преобразовать переданное значение в тип enum ticket_status
        String sql = """
            SELECT t.id,
                   t.user_id,
                   p.title AS performance_title,
                   t.row_number,
                   t.seat_number,
                   t.status,
                   t.created_at
            FROM Ticket t
            JOIN Performance p
                ON p.id = t.performance_id
            WHERE t.user_id = ?
              AND t.status = ?::ticket_status
            ORDER BY t.created_at DESC
            """;

        List<Ticket> tickets = new ArrayList<>();

        // Подключение к БД
        // try-with-resource
        // statement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // В первый ? установить значение id пользователя
            statement.setInt(1, userId);

            // Во второй ? установить статус билета
            statement.setString(2, statusToDatabase(status));

            try (ResultSet resultSet = statement.executeQuery()) {

                // Добавление билета в список
                // Вызов next() перемещает указатель на следующую строку
                while (resultSet.next()) {
                    tickets.add(mapTicket(resultSet));
                }
            }
        }

        return tickets;
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    @Override
    public List<Ticket> findByPerformanceStatus(
            int userId,
            String performanceStatus
    ) throws SQLException {

        // Объявление запроса
        String sql;

        // SQL запрос для спектаклей, которые ещё не завершились.
        // Равен ли performanceStatus строке "upcoming" без учёта регистра?
        if ("upcoming".equalsIgnoreCase(performanceStatus)) {

            sql = """
                SELECT t.id,
                       t.user_id,
                       p.title AS performance_title,
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

            // SQL запрос для спектаклей, которые уже завершились.
            sql = """
                SELECT t.id,
                       t.user_id,
                       p.title AS performance_title,
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

            // Если передан неизвестный статус спектакля,
            // выбрасывается ошибка.
            throw new IllegalArgumentException(
                    "Неизвестный статус спектакля: " + performanceStatus
            );
        }

        return executeTicketListQuery(sql, userId);
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод возвращает билеты, отсортированные по статусу.
    // Сортировка по умолчанию:
    // paid -> booked -> canceled
    @Override
    public List<Ticket> findAllSortedByStatus(int userId)
            throws SQLException {

        String sql = """
            SELECT t.id,
                   t.user_id,
                   p.title AS performance_title,
                   t.row_number,
                   t.seat_number,
                   t.status,
                   t.created_at
            FROM Ticket t
            JOIN Performance p
                ON p.id = t.performance_id
            WHERE t.user_id = ?
            ORDER BY
                CASE t.status
                    WHEN 'paid' THEN 1
                    WHEN 'booked' THEN 2
                    WHEN 'canceled' THEN 3
                END,
                t.created_at DESC
            """;

        return executeTicketListQuery(sql, userId);
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод возвращает билеты, отсортированные по статусу.
    // ascending = true — paid -> booked -> canceled
    // ascending = false — canceled -> booked -> paid
    @Override
    public List<Ticket> findAllSortedByStatus(
            int userId,
            boolean ascending
    ) throws SQLException {

        // Выбор направления сортировки.
        String sortDirection = ascending ? "ASC" : "DESC";

        String sql = """
            SELECT t.id,
                   t.user_id,
                   p.title AS performance_title,
                   t.row_number,
                   t.seat_number,
                   t.status,
                   t.created_at
            FROM Ticket t
            JOIN Performance p
                ON p.id = t.performance_id
            WHERE t.user_id = ?
            ORDER BY
                CASE t.status
                    WHEN 'paid' THEN 1
                    WHEN 'booked' THEN 2
                    WHEN 'canceled' THEN 3
                END
            """ + sortDirection + """
            ,
                t.created_at DESC
            """;

        return executeTicketListQuery(sql, userId);
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод возвращает билеты, отсортированные по дате спектакля.
    // По умолчанию — от недавних спектаклей к старым.
    @Override
    public List<Ticket> findAllSortedByDate(int userId)
            throws SQLException {

        String sql = """
            SELECT t.id,
                   t.user_id,
                   p.title AS performance_title,
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


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод возвращает билеты, отсортированные по дате спектакля.
    // ascending = true — от старых спектаклей к новым.
    // ascending = false — от новых спектаклей к старым.
    @Override
    public List<Ticket> findAllSortedByDate(
            int userId,
            boolean ascending
    ) throws SQLException {

        // Выбор направления сортировки.
        String sortDirection = ascending ? "ASC" : "DESC";

        String sql = """
            SELECT t.id,
                   t.user_id,
                   p.title AS performance_title,
                   t.row_number,
                   t.seat_number,
                   t.status,
                   t.created_at
            FROM Ticket t
            JOIN Performance p
                ON p.id = t.performance_id
            WHERE t.user_id = ?
            ORDER BY p.performance_date
            """ + sortDirection;

        return executeTicketListQuery(sql, userId);
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод ищет билеты пользователя по названию спектакля.
    @Override
    public List<Ticket> findByPerformanceTitle(
            int userId,
            String performanceTitle
    ) throws SQLException {

        // LOWER — переводит текст в нижний регистр.
        // LIKE — позволяет искать совпадение по части строки.
        // % означает любое количество символов.
        String sql = """
            SELECT t.id,
                   t.user_id,
                   p.title AS performance_title,
                   t.row_number,
                   t.seat_number,
                   t.status,
                   t.created_at
            FROM Ticket t
            JOIN Performance p
                ON p.id = t.performance_id
            WHERE t.user_id = ?
              AND LOWER(p.title) LIKE LOWER(?)
            ORDER BY p.performance_date DESC
            """;

        List<Ticket> tickets = new ArrayList<>();

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // В первый ? установить id пользователя
            statement.setInt(1, userId);

            // Во второй ? установить название спектакля.
            // % позволяет искать не только полное название,
            // но и часть названия.
            statement.setString(
                    2,
                    "%" + performanceTitle + "%"
            );

            try (ResultSet resultSet = statement.executeQuery()) {

                // Добавление найденного билета в список.
                while (resultSet.next()) {
                    tickets.add(mapTicket(resultSet));
                }
            }
        }

        return tickets;
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    @Override
    public Optional<Ticket> findById(
            int id,
            int userId
    ) throws SQLException {

        // ? — плейсхолдер
        String sql = """
            SELECT t.id,
                   t.user_id,
                   p.title AS performance_title,
                   t.row_number,
                   t.seat_number,
                   t.status,
                   t.created_at
            FROM Ticket t
            JOIN Performance p
                ON p.id = t.performance_id
            WHERE t.id = ?
              AND t.user_id = ?
            """;

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // В первый ? установить значение id билета
            statement.setInt(1, id);

            // Во второй ? установить id пользователя
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                // Существует ли билет
                if (resultSet.next()) {
                    return Optional.of(mapTicket(resultSet));
                }
            }
        }

        return Optional.empty();
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    @Override
    public boolean updateStatus(
            int id,
            int userId,
            TicketStatus newStatus
    ) throws SQLException {

        // ? — плейсхолдер
        // Разрешено:
        // booked -> paid
        // booked -> canceled
        // paid -> canceled
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

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String status = statusToDatabase(newStatus);

            statement.setString(1, status);
            statement.setInt(2, id);
            statement.setInt(3, userId);
            statement.setString(4, status);
            statement.setString(5, status);

            // executeUpdate используется для INSERT, UPDATE, DELETE
            // и возвращает количество изменённых строк
            return statement.executeUpdate() > 0;
        }
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    @Override
    public boolean deleteById(
            int id,
            int userId
    ) throws SQLException {

        // ? — плейсхолдер
        String sql = """
            DELETE FROM Ticket
            WHERE id = ?
              AND user_id = ?
            """;

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Вставка в плейсхолдеры
            statement.setInt(1, id);
            statement.setInt(2, userId);

            // executeUpdate используется для INSERT, UPDATE, DELETE
            // и возвращает количество изменённых строк
            return statement.executeUpdate() > 0;
        }
    }


    // Аннотация @Override метод класса переопределяет метод интерфейса.
    @Override
    public Ticket create(
            int userId,
            int performanceId,
            int rowNumber,
            int seatNumber
    ) throws SQLException {

        // ? — плейсхолдеры
        // performanceId нужен здесь для создания связи
        // билета со спектаклем в таблице Ticket.
        String sql = """
            INSERT INTO Ticket (
                user_id,
                performance_id,
                row_number,
                seat_number,
                status
            )
            VALUES (?, ?, ?, ?, 'booked')
            RETURNING id
            """;

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Вставка в плейсхолдеры
            statement.setInt(1, userId);
            statement.setInt(2, performanceId);
            statement.setInt(3, rowNumber);
            statement.setInt(4, seatNumber);

            try (ResultSet resultSet = statement.executeQuery()) {

                // Получение id только что созданного билета.
                if (resultSet.next()) {

                    int ticketId = resultSet.getInt("id");

                    Optional<Ticket> ticket = findById(ticketId, userId);

                    if (ticket.isPresent()) {
                        return ticket.get();
                    }
                }
            }

            // Обработка занятого места
        } catch (SQLException e) {

            // Код 23505 — нарушение UNIQUE-ограничения PostgreSQL.
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


    // Метод выполняет переданный SQL-запрос
    // и преобразует найденные строки в список Ticket.
    private List<Ticket> executeTicketListQuery(
            String sql,
            int userId
    ) throws SQLException {

        List<Ticket> tickets = new ArrayList<>();

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                // Каждая строка становится Ticket
                while (resultSet.next()) {
                    tickets.add(mapTicket(resultSet));
                }
            }
        }

        return tickets;
    }


    // Метод создания из строки ResultSet объект Ticket
    private Ticket mapTicket(ResultSet resultSet)
            throws SQLException {

        Ticket ticket = new Ticket();

        ticket.setId( resultSet.getInt("id") );

        ticket.setUserId( resultSet.getInt("user_id") );

        ticket.setPerformanceTitle( resultSet.getString("performance_title") );

        ticket.setRowNumber( resultSet.getInt("row_number"));

        ticket.setSeatNumber( resultSet.getInt("seat_number") );

        ticket.setStatus(TicketStatus.valueOf(resultSet.getString("status").toUpperCase()));

        ticket.setCreatedAt(resultSet.getString("created_at"));

        return ticket;
    }


    // Метод преобразует Java enum TicketStatus
    // в строку, которая используется в PostgreSQL.
    private String statusToDatabase(TicketStatus status) {

        return switch (status) {
            case BOOKED -> "booked";
            case PAID -> "paid";
            case CANCELED -> "canceled";
        };
    }

}