package repository;

import model.Performance;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PerformanceRepository {

    List<Performance> findAll() throws SQLException;

    Optional<Performance> findById(int id) throws SQLException;
}