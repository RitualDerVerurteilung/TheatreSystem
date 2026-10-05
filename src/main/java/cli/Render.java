package cli;

import model.Performance;
import model.Ticket;

public class Render {

    public static void showShortPerformance(Performance performance) {
        String output = "+------------------------------+\n"
                + performance.getTitle() + " | " + performance.getDate() + " | ID: " + performance.getId()
                + "\n+------------------------------+\n"
                + performance.getDescription().substring(0, 100) + "..."
                + "\n+------------------------------+\n"
                + "    Цена: " + performance.getPrice() + "\n";
        System.out.println(output);
    }

    public static void showFullPerformance(Performance performance, int[][] seats) {
        showShortPerformance(performance);

        showPerformanceSeats(seats);
        System.out.println();
    }

    static void showPerformanceSeats(int[][] seats) {
        System.out.print("       ");

        for (int seat = 1; seat <= 20; seat++) {
            System.out.printf("%3d ", seat);
        }

        System.out.println();

        for (int row = 0; row < 15; row++) {

            System.out.printf(
                    "Ряд %2d: ",
                    row + 1
            );

            for (int seat = 0; seat < 20; seat++) {
                System.out.print(
                        seats[row][seat] == 1 ? "[x] " : "[ ] "
                );
            }

            System.out.println();
        }
    }

    public static void showTicket(Ticket ticket) {
        System.out.println(
                "ID: "
                        + ticket.getId()
                        + " | Спектакль: "
                        + ticket.getPerformanceTitle()
                        + " | Ряд: "
                        + ticket.getRowNumber()
                        + " | Место: "
                        + ticket.getSeatNumber()
                        + " | Статус: "
                        + ticket.getStatus()
                        + " | Создан: "
                        + ticket.getCreatedAt()
        );
    }
}
