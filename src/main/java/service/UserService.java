package service;

import cli.ConsoleInput;
import model.User;
import repository.UserRepository;

import java.sql.SQLException;
import java.util.Optional;


public class UserService {


    UserRepository userRepository;

    public UserService(UserRepository userRepository) throws SQLException {
        this.userRepository = userRepository;
    }


    public User mainService() {
        while (true) {

            String action = ConsoleInput.input(
                    "Введите действие:\n" +
                            "1 - Зарегистрироваться,\n" +
                            "2 - Залогиниться\n" +
                            "3 - Выход",
                    "ThreeOptions");

            switch (action) {
                case "1" -> { return registration(); }
                case "2" -> { return login(); }
                case "3" -> { return null; }
            }
        }
    }

    User registration() {
        String firstName = ConsoleInput.input("Введите ваше имя: ", "No");
        String lastName = ConsoleInput.input("Введите вашу фамилию: ", "No");
        String email = ConsoleInput.input("Введите ваш email: ", "Email");
        String passport = ConsoleInput.input("Введите серию и номер паспорта через пробел: ", "Passport");
        String password = ConsoleInput.input("Введите ваш пароль: ", "No");
        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPassportData(passport);
        newUser.setPasswordHash(password);

        try {
            newUser = userRepository.create(newUser);
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }

        return newUser;
    }

    User login() {
        String email = ConsoleInput.input("Введите ваш email: ", "Email");
        String password = ConsoleInput.input("Введите ваш пароль: ", "No");

        try {
            Optional<User> user = userRepository.findByEmail(email);
            return user.orElse(null);
        } catch (SQLException e) {
            System.out.println("Возникла непредвиденная ошибка в процессе запроса к базе данных: " + e);
        }

        return null;
    }
}
