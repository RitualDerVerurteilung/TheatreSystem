import cli.ConsoleInput;
import model.User;
import repository.UserRepository;
import repository.implementation.UserRepositoryImpl;
import service.TicketService;
import service.UserService;
import util.DatabaseManager;
import model.Performance;
import model.Ticket;
import repository.PerformanceRepository;
import repository.TicketRepository;
import repository.implementation.PerformanceRepositoryImpl;
import repository.implementation.TicketRepositoryImpl;
import service.PerformanceService;
import util.ExcelExporter;
import util.Statistic;

import java.sql.SQLException;
import java.util.List;

public class Program {
    public static void main(String[] args) {

        DatabaseManager databaseManager = new DatabaseManager(
                "jdbc:postgresql://localhost:5432/Theatre",
                "postgres",
                "root"
        );

        databaseManager.isConnected();

        int userId = 0;

        UserRepository userRepository = new UserRepositoryImpl(databaseManager);


        try {
            UserService userService = new UserService(userRepository);
            User currentUser = userService.mainService();
            userId = currentUser.getId();
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
            return;
        }


        try {
            PerformanceRepository performanceRepository =
                    new PerformanceRepositoryImpl(databaseManager);

            TicketRepository ticketRepository =
                    new TicketRepositoryImpl(databaseManager);


            ExcelExporter exporter = new ExcelExporter(userId, performanceRepository, ticketRepository);



            PerformanceService performanceService = new PerformanceService(performanceRepository, ticketRepository, userId);

            TicketService ticketService = new TicketService(performanceRepository, ticketRepository, userId);

            String action = ConsoleInput.input(
                    "Введите действие:\n" +
                            "1 - Спектакли,\n" +
                            "2 - Билеты\n" +
                            "3 - Статистика\n" +
                            "4 - Экспорт спектаклей и купленных билетов\n" +
                            "5 - Вывести таблицы базы данных\n" +
                            "6 - Выход",
                    "SixOptions");

            switch (action) {
                case "1" -> {
                    performanceService.mainService();
                }
                case "2" -> {
                    ticketService.mainService();
                }
                case "3" -> {
                    Statistic stat = new Statistic(databaseManager);
                    stat.printStatistics();
                }
                case "4" -> {
                    exporter.export();
                }
                case "5" -> {
                    printSchema();
                }
                case "6" -> {
                    return;
                }
            }





        } catch (Exception e) {
            System.out.println("Ошибочка: " + e);
        }
    }


    public static void printSchema() {
        System.out.println("""
            ┌──────────────────────────────────────────────┐
            │                 User_Theatre                 │
            ├──────────────────────────────────────────────┤
            │ PK  id              serial                   │
            │     first_name      text        NOT NULL     │
            │     last_name       text        NOT NULL     │
            │ UQ  email           text        NOT NULL     │
            │ UQ  passport_data   varchar(11) NOT NULL     │
            │     password_hash   text        NOT NULL     │
            └──────────────────────────────────────────────┘

            ┌──────────────────────────────────────────────┐
            │                 Performance                  │
            ├──────────────────────────────────────────────┤
            │ PK  id               serial                  │
            │     title            varchar(255) NOT NULL   │
            │     description      text                    │
            │     performance_date timestamptz  NOT NULL   │
            │     duration         int          NOT NULL   │
            │     base_price       numeric(10,2) NOT NULL  │
            └──────────────────────────────────────────────┘

            ┌──────────────────────────────────────────────┐
            │                    Ticket                    │
            ├──────────────────────────────────────────────┤
            │ PK  id             serial                    │
            │ FK  user_id        int  → User_Theatre.id    │
            │ FK  performance_id int  → Performance.id     │
            │     row_number     int  1..15        NOT NULL│
            │     seat_number    int  1..20        NOT NULL│
            │     status         ticket_status     NOT NULL│
            │     created_at     timestamptz       NOT NULL│
            └──────────────────────────────────────────────┘
            """);
    }

}
//
//import util.DatabaseManager;
//import model.Performance;
//import model.Ticket;
//import repository.PerformanceRepository;
//import repository.TicketRepository;
//import repository.implementation.PerformanceRepositoryImpl;
//import repository.implementation.TicketRepositoryImpl;
//import util.Statistic;
//
//import java.util.List;
//import java.sql.SQLException;
//public class Program {
//
//    // Размеры зала: 15 рядов по 20 мест
//    private static final int ROWS = 15;
//    private static final int SEATS_PER_ROW = 20;
//
//    public static void main(String[] args) {
//
//        DatabaseManager databaseManager = new DatabaseManager(
//                "jdbc:postgresql://localhost:5432/Theatre",
//                "postgres",
//                "root"
//        );
//
//        System.out.println(
//                "Подключение: " +
//                        databaseManager.isConnected()
//        );
//
//        try {
//            PerformanceRepository performanceRepository =
//                    new PerformanceRepositoryImpl(databaseManager);
//
//            TicketRepository ticketRepository =
//                    new TicketRepositoryImpl(databaseManager);
//
//            // Спектакли
//
//            List<Performance> performances =
//                    performanceRepository.findAll();
//
//            System.out.println("\nСпектакли по дате:");
//
//            for (Performance performance : performances) {
//                System.out.println(
//                        performance.getId()
//                                + " | "
//                                + performance.getTitle()
//                                + " | "
//                                + performance.getDate()
//                                + " | "
//                                + performance.getPrice()
//                );
//            }
//
//
//            // Матрица мест
//
//            for (Performance performance : performances) {
//
//                int performanceId =
//                        performance.getId();
//
//                // Получаем список занятых мест в виде пар {ряд, место}
//                List<int[]> occupiedSeats =
//                        performanceRepository.findOccupiedSeats(performanceId);
//
//                // Строим матрицу 0/1:
//                // 0 — свободно, 1 — занято
//                int[][] seats = new int[ROWS][SEATS_PER_ROW];
//
//                for (int[] occupiedSeat : occupiedSeats) {
//
//                    int row = occupiedSeat[0];
//                    int seatNumber = occupiedSeat[1];
//
//                    // Проверка на корректность координат
//                    seats[row - 1][seatNumber - 1] = 1;
//                }
//
//                System.out.println(
//                        "\nМатрица мест спектакля "
//                                + performanceId
//                                + " ("
//                                + performance.getTitle()
//                                + "):"
//                );
//
//                // Заголовок с номерами мест
//                System.out.print("       ");
//
//                for (int seat = 1; seat <= SEATS_PER_ROW; seat++) {
//                    System.out.printf("%2d ", seat);
//                }
//
//                System.out.println();
//
//                // Сама матрица
//                for (int row = 0; row < ROWS; row++) {
//
//                    System.out.printf(
//                            "Ряд %2d: ",
//                            row + 1
//                    );
//
//                    for (int seat = 0; seat < SEATS_PER_ROW; seat++) {
//                        System.out.print(
//                                seats[row][seat] + "  "
//                        );
//                    }
//
//                    System.out.println();
//                }
//            }
//
//
//            // Билеты пользователя
//
//            // Для теста используется пользователь с id = 1.
//            int userId = 1;
//
//            // Сортировка по статусу
//            System.out.println(
//                    "\nБилеты пользователя "
//                            + userId
//                            + " по статусу:"
//            );
//
//            List<Ticket> ticketsByStatus =
//                    ticketRepository.findAllSortedByStatus(userId);
//
//            printTickets(ticketsByStatus);
//
//
//            System.out.println(
//                    "\nБилеты по статусу в обратном порядке:"
//            );
//
//            List<Ticket> ticketsByStatusReverse =
//                    ticketRepository.findAllSortedByStatus(
//                            userId,
//                            false
//                    );
//
//            printTickets(ticketsByStatusReverse);
//
//            // Сортировка по дате
//            System.out.println(
//                    "\nБилеты по дате спектакля "
//                            + "(от новых к старым):"
//            );
//
//            List<Ticket> ticketsByDate =
//                    ticketRepository.findAllSortedByDate(userId);
//
//            printTickets(ticketsByDate);
//
//
//            System.out.println(
//                    "\nБилеты по дате спектакля "
//                            + "(от старых к новым):"
//            );
//
//            List<Ticket> ticketsByDateReverse =
//                    ticketRepository.findAllSortedByDate(
//                            userId,
//                            true
//                    );
//
//            printTickets(ticketsByDateReverse);
//
//
//            // Поиск по названию
//            String performanceTitle = "Гамлет";
//
//            System.out.println(
//                    "\nПоиск билетов по названию спектакля: "
//                            + performanceTitle
//            );
//
//            List<Ticket> foundTickets =
//                    ticketRepository.findByPerformanceTitle(
//                            userId,
//                            performanceTitle
//                    );
//
//            printTickets(foundTickets);
//
//            // Билет по id
//            int ticketId = 1;
//
//            System.out.println(
//                    "\nПоиск билета по ID: "
//                            + ticketId
//            );
//
//            ticketRepository.findById(
//                    ticketId,
//                    userId
//            ).ifPresentOrElse(
//                    ticket -> printTicket(ticket),
//                    () -> System.out.println(
//                            "Билет не найден."
//                    )
//            );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Statistic statisticsService = new Statistic(databaseManager);
//        try {
//            statisticsService.printStatistics();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    // Метод выводит список билетов.
//    private static void printTickets(List<Ticket> tickets) {
//
//        if (tickets.isEmpty()) {
//            System.out.println("Билетов нет.");
//            return;
//        }
//
//        for (Ticket ticket : tickets) {
//
//            System.out.println(
//                    "ID: "
//                            + ticket.getId()
//                            + " | Спектакль: "
//                            + ticket.getPerformanceTitle()
//                            + " | Ряд: "
//                            + ticket.getRowNumber()
//                            + " | Место: "
//                            + ticket.getSeatNumber()
//                            + " | Статус: "
//                            + ticket.getStatus()
//                            + " | Создан: "
//                            + ticket.getCreatedAt()
//            );
//        }
//    }
//
//
//    // Метод выводит один билет.
//    private static void printTicket(Ticket ticket) {
//
//        System.out.println(
//                "ID: "
//                        + ticket.getId()
//                        + " | Спектакль: "
//                        + ticket.getPerformanceTitle()
//                        + " | Ряд: "
//                        + ticket.getRowNumber()
//                        + " | Место: "
//                        + ticket.getSeatNumber()
//                        + " | Статус: "
//                        + ticket.getStatus()
//                        + " | Создан: "
//                        + ticket.getCreatedAt()
//        );
//    }
//}