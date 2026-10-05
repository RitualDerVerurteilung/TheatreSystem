package service;

import model.Performance;
import model.Ticket;
import repository.PerformanceRepository;
import repository.TicketRepository;
import cli.Render;
import cli.ConsoleInput;

import java.sql.SQLException;
import java.util.List;


public class PerformanceService {

    List<Performance> performanceList;

    TicketRepository ticketRepository;
    PerformanceRepository performanceRepository;

    int userId;

    public PerformanceService(PerformanceRepository performanceRepository, TicketRepository ticketRepository, int userId) throws SQLException {
        this.performanceRepository = performanceRepository;
        this.ticketRepository = ticketRepository;

        this.performanceList = performanceRepository.findAll();
        this.userId = userId;
    }


    public void mainService() {
        while (true) {
            int page = 1;

            showPerformances();

            String action = ConsoleInput.input(
                    "Введите действие:\n" +
                            "1 - Посмотреть ещё 5 спектаклей,\n" +
                            "2 - Выбрать спектакль для заказа билета\n" +
                            "3 - Выход",
                    "ThreeOptions");

            switch (action) {
                case "1" -> showPerformances();
                case "2" -> operatePerformance();
                case "3" -> { return; }
            }
        }
    }


    public void showPerformances() {
        for (int i = 0; i < 5; i++) {
            Render.showShortPerformance(performanceList.get(i));
        }
    }

    void operatePerformance() {
        int id;
        while(true) {
            id = Integer.parseInt(ConsoleInput.input("\nВведите ID понравившегося спектакля:", "IntPositive"));
            boolean existingId = false;
            for (Performance perf : performanceList) {
                if (perf.getId() == id) {
                    existingId = true;
                    break;
                }
            }
            if (!existingId) {
                System.out.println("Введён несуществующий ID!");
            } else {
                break;
            }
        }

        final int idForLambda = id;
        Performance currentPerformance = performanceList.stream()
                .filter(p -> p.getId() == idForLambda)
                .findFirst()
                .get();

        try {
            List<int[]> occupiedSeats = performanceRepository.findOccupiedSeats(id);


            int[][] seats = new int[15][20];

            for (int[] occupiedSeat : occupiedSeats) {

                int row = occupiedSeat[0];
                int seatNumber = occupiedSeat[1];

                seats[row - 1][seatNumber - 1] = 1;
            }

            Render.showFullPerformance(currentPerformance, seats);

            handleTicketBuy(currentPerformance.getId(), seats);
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
    }

    void handleTicketBuy(int performanceId, int[][] seats) {
        int row;
        int seat;
        while (true) {
            row = Integer.parseInt(ConsoleInput.input("\nВведите ряд желаемого места:", "Row"));
            seat = Integer.parseInt(ConsoleInput.input("\nВведите номер желаемого места:", "Seat"));

            if (seats[row - 1][seat - 1] == 0) {
                break;
            } else {
                System.out.println("Место занято!");
            }
        }
        try {
            Ticket newTicket = ticketRepository.create(userId, performanceId, row, seat);

            System.out.println("Билет забронирован на ваше имя!\n");

            String payNow = ConsoleInput.input("Оплатить сейчас? Да/Нет", "Y/N");

            if (payNow.equals("Да")) {
                payForTicket(newTicket.getId(), userId);
            } else {
                System.out.println("Пожалуйста, оплатите билет до спектакля в сервисе билетов\n");
            }

        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
    }

    void payForTicket(int ticketId, int userId) {
        ConsoleInput.input("Введите номер вашей карты: ","Card");
        ConsoleInput.input("Введите дату истечения вашей карты в формате ММ/ГГ: ","MM/YY");
        ConsoleInput.input("Введите CVV/CVC вашей карты: ","CVV");
        try {
            ticketRepository.updateStatus(ticketId, userId, Ticket.TicketStatus.PAID);
            System.out.println("Оплата прошла успешно!\n");
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
    }
}
