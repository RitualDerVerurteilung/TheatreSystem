import cli.ConsoleInput;
import model.User;
import repository.UserRepository;
import repository.implementation.UserRepositoryImpl;
import service.TicketService;
import service.UserService;
import util.DatabaseManager;
import util.DbConfig;
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
                DbConfig.url(),
                DbConfig.user(),
                DbConfig.password()
        );

        databaseManager.isConnected();

        int userId = 0;

        UserRepository userRepository = new UserRepositoryImpl(databaseManager);


        try {
            UserService userService = new UserService(userRepository);
            User currentUser = userService.mainService();

            if (currentUser == null) {
                System.out.println("Авторизация не выполнена. Завершение работы.");
                return;
            }

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