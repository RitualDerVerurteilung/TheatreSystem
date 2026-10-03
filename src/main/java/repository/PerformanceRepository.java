package repository;

import model.Performance;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PerformanceRepository {

    // Метод возвращает весь список спектаклей
    List<Performance> findAll() throws SQLException;

    // Метод возвращает один спектакль по id
    Optional<Performance> findById(int id) throws SQLException;

    // Метод возвращает список занятых мест конкретного спектакля
    List<int[]> findOccupiedSeats(int performanceId)
            throws SQLException;
}