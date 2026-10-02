package repository.implementation;

import util.DatabaseManager;
import model.Performance;
import repository.PerformanceRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PerformanceRepositoryImpl implements PerformanceRepository {

    private final DatabaseManager databaseManager;

    public PerformanceRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

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

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                performances.add(mapPerformance(resultSet));
            }
        }

        return performances;
    }

    @Override
    public Optional<Performance> findById(int id) throws SQLException {
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

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapPerformance(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    private Performance mapPerformance(ResultSet resultSet)
            throws SQLException {

        Performance performance = new Performance();

        performance.setId(resultSet.getInt("id"));
        performance.setTitle(resultSet.getString("title"));
        performance.setDescription(resultSet.getString("description"));
        performance.setDate(
                resultSet.getString("performance_date")
        );
        performance.setDuration(resultSet.getInt("duration"));
        performance.setPrice(
                resultSet.getDouble("base_price")
        );

        return performance;
    }
}