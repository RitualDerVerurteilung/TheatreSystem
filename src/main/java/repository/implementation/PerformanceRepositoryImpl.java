package repository.implementation;

import util.DatabaseManager;
import model.Performance;
import repository.PerformanceRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PerformanceRepositoryImpl implements PerformanceRepository {

    // final — после присваивания значения поле нельзя заменить
    // т.е. один раз передаётся объект подключения
    private final DatabaseManager databaseManager;

    // Конструктор
    public PerformanceRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод возвращает весь список спектаклей.
    @Override
    public List<Performance> findAll() throws SQLException {

        String sql = """
                SELECT id,
                       title,
                       description,
                       performance_date,
                       duration,
                       base_price
                FROM Performance
                ORDER BY performance_date
                """;

        List<Performance> performances = new ArrayList<>();

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // Добавление спектакля в список
            // Вызов next() перемещает указатель на следующую строку
            while (resultSet.next()) {
                performances.add(mapPerformance(resultSet));
            }
        }

        return performances;
    }

    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод возвращает один спектакль по его id.
    @Override
    public Optional<Performance> findById(int id) throws SQLException {

        // ? — плейсхолдер
        String sql = """
                SELECT id,
                       title,
                       description,
                       performance_date,
                       duration,
                       base_price
                FROM Performance
                WHERE id = ?
                """;

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // В первый ? установить значение id спектакля
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                // Существует ли спектакль
                if (resultSet.next()) {
                    return Optional.of(mapPerformance(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    // Аннотация @Override метод класса переопределяет метод интерфейса.
    // Метод возвращает список занятых мест конкретного спектакля.
    @Override
    public List<int[]> findOccupiedSeats(int performanceId)
            throws SQLException {

        // ? — плейсхолдер
        String sql = """
                SELECT row_number,
                       seat_number
                FROM Ticket
                WHERE performance_id = ?
                  AND status IN ('booked', 'paid')
                ORDER BY row_number, seat_number
                """;

        List<int[]> occupiedSeats = new ArrayList<>();

        // Connection устанавливает соединение с БД
        // PreparedStatement — предварительно скомпилированный SQL-запрос
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // В первый ? установить id спектакля
            statement.setInt(1, performanceId);

            try (ResultSet resultSet = statement.executeQuery()) {

                // Переход по всем найденным занятым местам
                while (resultSet.next()) {

                    // Получение номера ряда из БД
                    int row = resultSet.getInt("row_number");

                    // Получение номера места из БД
                    int seat = resultSet.getInt("seat_number");

                    // Добавление занятого места в список.
                    occupiedSeats.add(
                            new int[]{row, seat}
                    );
                }
            }
        }

        // Возвращение списка занятых мест
        return occupiedSeats;
    }

    // Метод создания из строки ResultSet объект Performance
    private Performance mapPerformance(ResultSet resultSet)
            throws SQLException {

        Performance performance = new Performance();

        performance.setId( resultSet.getInt("id") );

        performance.setTitle( resultSet.getString("title") );

        performance.setDescription( resultSet.getString("description") );

        performance.setDate( resultSet.getString("performance_date") );

        performance.setDuration( resultSet.getInt("duration") );

        performance.setPrice( resultSet.getDouble("base_price") );

        return performance;
    }
}