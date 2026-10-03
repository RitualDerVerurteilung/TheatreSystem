package repository;

import model.Ticket;
import model.Ticket.TicketStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TicketRepository {

    List<Ticket> findByTicketStatus(
            int userId,
            TicketStatus status
    ) throws SQLException;

    List<Ticket> findByPerformanceStatus(
            int userId,
            String performanceStatus
    ) throws SQLException;

    List<Ticket> findAllSortedByStatus(
            int userId
    ) throws SQLException;

    List<Ticket> findAllSortedByStatus(
            int userId,
            boolean ascending
    ) throws SQLException;

    List<Ticket> findAllSortedByDate(
            int userId
    ) throws SQLException;

    List<Ticket> findAllSortedByDate(
            int userId,
            boolean ascending
    ) throws SQLException;

    List<Ticket> findByPerformanceTitle(
            int userId,
            String performanceTitle
    ) throws SQLException;

    Optional<Ticket> findById(
            int id,
            int userId
    ) throws SQLException;

    boolean updateStatus(
            int id,
            int userId,
            TicketStatus newStatus
    ) throws SQLException;

    boolean deleteById(
            int id,
            int userId
    ) throws SQLException;

    Ticket create(
            int userId,
            int performanceId,
            int rowNumber,
            int seatNumber
    ) throws SQLException;
}