package repository;

import model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserRepository {
    User create(User user) throws SQLException;
    Optional<User> findByEmailAndPassword(String email, String password) throws SQLException;
}