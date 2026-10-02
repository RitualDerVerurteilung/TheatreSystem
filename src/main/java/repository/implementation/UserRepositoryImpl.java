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

                    createdUser.setId(
                            resultSet.getInt("id")
                    );

                    createdUser.setFirstName(
                            resultSet.getString("first_name")
                    );

                    createdUser.setLastName(
                            resultSet.getString("last_name")
                    );

                    createdUser.setEmail(
                            resultSet.getString("email")
                    );

                    createdUser.setPassportData(
                            resultSet.getString("passport_data")
                    );

                    createdUser.setPasswordHash(
                            resultSet.getString("password_hash")
                    );

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
    public Optional<User> findByEmail(String email)
            throws SQLException {

        String sql = """
                SELECT id,
                       first_name,
                       last_name,
                       email,
                       passport_data,
                       password_hash
                FROM User_Theatre
                WHERE email = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    User user = new User();

                    user.setId(
                            resultSet.getInt("id")
                    );

                    user.setFirstName(
                            resultSet.getString("first_name")
                    );

                    user.setLastName(
                            resultSet.getString("last_name")
                    );

                    user.setEmail(
                            resultSet.getString("email")
                    );

                    user.setPassportData(
                            resultSet.getString("passport_data")
                    );

                    user.setPasswordHash(
                            resultSet.getString("password_hash")
                    );

                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }
}