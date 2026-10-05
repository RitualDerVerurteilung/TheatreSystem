package service;

import cli.ConsoleInput;
import cli.Render;
import model.Performance;
import model.Ticket;
import model.Ticket.TicketStatus;
import repository.PerformanceRepository;
import repository.TicketRepository;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TicketService {

    List<Ticket> ticketList;

    TicketRepository ticketRepository;

    int userId;

    int page = 0;

    public TicketService(PerformanceRepository performanceRepository, TicketRepository ticketRepository, int userId) throws SQLException {
        this.ticketRepository = ticketRepository;

        this.ticketList = ticketRepository.findAllSortedByDate(userId);

        this.userId = userId;
    }


    public void mainService() {
        while (true) {

            showTickets(page);

            String action = ConsoleInput.input(
                    "Введите действие:\n" +
                            "1 - Посмотреть ещё 5 билетов,\n" +
                            "2 - Поиск по названию спектакля\n" +
                            "3 - Фильтрация\n" +
                            "4 - Сортировка\n" +
                            "5 - Билет по ID\n" +
                            "6 - Выход",
                    "SixOptions");

            switch (action) {
                case "1" -> {
                    boolean reset = showTickets(page);
                    if (reset) {
                        page = 0;
                    } else {
                        page += 1;
                    }
                }
                case "2" -> {
                    List<Ticket> newList = findByPerformanceTitle(userId);
                    ticketList = newList;
                    page = 0;
                }
                case "3" -> {
                    List<Ticket> newList = filter(userId);
                    ticketList = newList;
                    page = 0;
                }
                case "4" -> {
                    List<Ticket> newList = sort(userId);
                    ticketList = newList;
                    page = 0;
                }
                case "5" -> {
                    ticketById(userId);

                    page = 0;
                }
                case "6" -> {
                    return;
                }
            }
        }
    }

    boolean showTickets(int page) {
        if (5 * page > ticketList.size() - 1) {
            System.out.println("Были выведены все билеты");
            return true;
        }

        int end = 5 * page + 5;
        if (end > ticketList.size()) {
            end = ticketList.size();
        }
        List<Ticket> listSlice = ticketList.subList(5 * page, end);
        for (Ticket ticket : listSlice) {
            Render.showTicket(ticket);
        }
        return false;
    }


    List<Ticket> findByPerformanceTitle(int userId) {
        String performanceTitle = ConsoleInput.input("Введите название спектакля: ", "No");
        try {
            List<Ticket> newList = ticketRepository.findByPerformanceTitle(userId, performanceTitle);
            if (newList.isEmpty()) {
                System.out.println("Не найдено спектаклей с таким именем!");
                return ticketList;
            }
            return newList;
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
        return ticketList;
    }

    List<Ticket> filter(int userId) {
        String action = ConsoleInput.input(
                "Выберите тип фильтрации:\n" +
                        "1 - По статусу билета,\n" +
                        "2 - По статусу спектакля\n" +
                        "3 - Выход",
                "ThreeOptions");
        switch (action) {
            case "1" -> {
                return filterByStatus(userId);
            }
            case "2" -> {
                return filterByPerformanceStatus(userId);
            }
            case "3" -> {
                return ticketList;
            }
        }
        return ticketList;
    }

    List<Ticket> filterByStatus(int userId) {
        String stringStatus = ConsoleInput.input("\nВведите статус для фильтра (booked, paid или canceled): ", "Status");
        TicketStatus status = TicketStatus.PAID;
        switch (stringStatus) {
            case "booked" -> {
                status = TicketStatus.BOOKED;
            }
            case "paid" -> {
                status = TicketStatus.PAID;
            }
            case "canceled" -> {
                status = TicketStatus.CANCELED;
            }
        }
        try {
            List<Ticket> newList = ticketRepository.findByTicketStatus(userId, status);
            return newList;
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
        return ticketList;
    }

    List<Ticket> filterByPerformanceStatus(int userId) {
        String status = ConsoleInput.input("\nВведите статус для фильтра (upcoming или finished): ", "PerformanceStatus");
        try {
            List<Ticket> newList = ticketRepository.findByPerformanceStatus(userId, status);
            return newList;
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
        return ticketList;
    }

    List<Ticket> sort(int userId) {
        String action = ConsoleInput.input(
                "Выберите тип сортировки:\n" +
                        "1 - По статусу билета,\n" +
                        "2 - По дате спектакля\n" +
                        "3 - Выход",
                "ThreeOptions");
        switch (action) {
            case "1" -> {
                return sortByStatus(userId);
            }
            case "2" -> {
                return sortByPerformanceDate(userId);
            }
            case "3" -> {
                return ticketList;
            }
        }
        return ticketList;
    }

    List<Ticket> sortByStatus(int userId) {
        try {
            List<Ticket> newList = ticketRepository.findAllSortedByStatus(userId);
            return newList;
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
        return ticketList;
    }

    List<Ticket> sortByPerformanceDate(int userId) {
        try {
            List<Ticket> newList = ticketRepository.findAllSortedByDate(userId);
            return newList;
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }
        return ticketList;
    }

    void ticketById(int userId) {
        int id;
        while (true) {
            id = Integer.parseInt(ConsoleInput.input("\nВведите ID билета:", "IntPositive"));
            boolean existingId = false;
            for (Ticket tick : ticketList) {
                if (tick.getId() == id) {
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
        Ticket currentTicket = ticketList.stream()
                .filter(t -> t.getId() == idForLambda)
                .findFirst()
                .get();

        String action = ConsoleInput.input(
                "Что вы хотите сделать с билетом:\n" +
                        "1 - Изменить билет,\n" +
                        "2 - Удалить билет из списка\n" +
                        "3 - Выход",
                "ThreeOptions");
        switch (action) {
            case "1" -> changeTicket(userId, currentTicket);
            case "2" -> deleteTicket(userId, currentTicket);
            case "3" -> {
                return;
            }
        }
    }

    void changeTicket(int userId, Ticket ticket) {
        String action = ConsoleInput.input(
                "Как вы хотите изменить билет:\n" +
                        "1 - Купить билет,\n" +
                        "2 - Отменить билет\n" +
                        "3 - Выход",
                "ThreeOptions");
        switch (action) {
            case "1" -> payForTicket(ticket, userId);
            case "2" -> cancelTicket(ticket, userId);
            case "3" -> {
                return;
            }
        }
    }

    void payForTicket(Ticket ticket, int userId) {
        if (ticket.getStatus() == TicketStatus.BOOKED) {
            ConsoleInput.input("Введите номер вашей карты: ", "Card");
            ConsoleInput.input("Введите дату истечения вашей карты в формате ММ/ГГ: ", "MM/YY");
            ConsoleInput.input("Введите CVV/CVC вашей карты: ", "CVV");
            try {
                ticketRepository.updateStatus(ticket.getId(), userId, Ticket.TicketStatus.PAID);
                System.out.println("Оплата прошла успешно!\n");
            } catch (SQLException e) {
                System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
            }
        } else {
            System.out.println("Вы не можете оплатить купленный или отменённый билет\n");
        }
    }

    void cancelTicket(Ticket ticket, int userId) {
        if (ticket.getStatus() == TicketStatus.BOOKED || ticket.getStatus() == TicketStatus.PAID) {
            String cancel = ConsoleInput.input("\nВы уверены? Да/Нет: ", "Y/N");
            if (cancel.equals("Да")) {
                try {
                    ticketRepository.updateStatus(ticket.getId(), userId, TicketStatus.CANCELED);
                    System.out.println("Отмена прошла успешно!\n");
                } catch (SQLException e) {
                    System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
                }
            }
        } else {
            System.out.println("Вы не можете отменить отменённый билет\n");
        }
    }

    void deleteTicket(int userId, Ticket ticket) {
        String cancel = ConsoleInput.input("\nУдаление билета приведёт к его недействительности. Вы уверены?  Да/Нет: ", "Y/N");
        if (cancel.equals("Да")) {
            try {
                ticketRepository.deleteById(ticket.getId(), userId);
                System.out.println("Удаление билета прошло успешно!\n");
            } catch (SQLException e) {
                System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
            }
        }
    }
}
