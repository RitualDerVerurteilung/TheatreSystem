package repository.implementation;

import util.DatabaseManager;
import model.User;
import repository.UserRepository;

import java.sql.*;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final DatabaseManager databaseManager;

    public UserRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public User create(User user) throws SQLException {

        String sql = """
                INSERT INTO User_Theatre (
                    first_name,
                    last_name,
                    email,
                    passport_data,
                    password_hash
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id,
                          first_name,
                          last_name,
                          email,
                          passport_data,
                          password_hash
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassportData());
            statement.setString(5, user.getPasswordHash());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    User createdUser = new User();
                    createdUser.setId( resultSet.getInt("id"));
                    createdUser.setFirstName(resultSet.getString("first_name"));
                    createdUser.setLastName(resultSet.getString("last_name"));
                    createdUser.setEmail(resultSet.getString("email"));
                    createdUser.setPassportData(resultSet.getString("passport_data"));
                    createdUser.setPasswordHash(resultSet.getString("password_hash"));
                    return createdUser;
                }
            }

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new SQLException(
                        "Пользователь с таким email или паспортными данными уже существует",
                        e
                );
            }

            throw e;
        }

        throw new SQLException("Не удалось создать пользователя");
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT id, name, email, password, role FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}