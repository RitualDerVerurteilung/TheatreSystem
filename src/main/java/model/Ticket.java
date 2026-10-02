package model;

public class Ticket {

    public enum TicketStatus {
        BOOKED,
        PAID,
        CANCELED
    }

    int id;
    int userId;
    int performanceId;
    int rowNumber;
    int seatNumber;
    TicketStatus status;
    String createdAt;


    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getUserId() {
        return userId;
    }

    public void setPerformanceId(int performanceId) {
        this.performanceId = performanceId;
    }
    public int getPerformanceId() {
        return performanceId;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
    public int getRowNumber() {
        return rowNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
    public int getSeatNumber() {
        return seatNumber;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
    public TicketStatus getStatus() {
        return status;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public String getCreatedAt() {
        return createdAt;
    }
}