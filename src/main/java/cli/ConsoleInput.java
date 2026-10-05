package cli;

import java.util.Scanner;

public class ConsoleInput {

    public static String input(String output, String restriction) {

        String resultString = "";

        try {
            System.out.println(output);

            while (true) {
                Scanner scanner = new Scanner(System.in);

                resultString = scanner.nextLine().strip();



                if (checkRestriction(resultString, restriction)) {
                    return resultString;
                }
                else {
                    System.out.println("\nВвод некорректен. Попробуйте ещё раз.\n" + output);
                }
            }

        } catch(Throwable e) {
            System.out.println("Возникла непредвиденная ошибка в процессе обработки вашего ввода: " + e);
        }
        return resultString;
    }

    static boolean checkRestriction(String inputString, String restriction) {
        switch (restriction) {

            case "ThreeOptions" -> {
                return inputString.equals("1") || inputString.equals("2") || inputString.equals("3");
            }

            case "Row" -> {
                try {
                    return Integer.parseInt(inputString) > 0 && Integer.parseInt(inputString) < 16;
                } catch (Throwable _) {
                    return false;
                }
            }

            case "Seat" -> {
                try {
                    return Integer.parseInt(inputString) > 0 && Integer.parseInt(inputString) < 21;
                } catch (Throwable _) {
                    return false;
                }
            }

            case "IntPositive" -> {
                try {
                    return Integer.parseInt(inputString) > 0;
                } catch (Throwable _) {
                    return false;
                }
            }

            case "Y/N" -> {
                return inputString.equals("Да") || inputString.equals("Нет");
            }

            case "Card" -> {
                return inputString.matches("\\d{16}");
            }

            case "MM/YY" -> {
                return inputString.matches("\\d{2}/\\d{2}");
            }

            case "CVV" -> {
                return inputString.matches("\\d{3}");
            }

            case "SixOptions" -> {
                return inputString.equals("1") || inputString.equals("2") || inputString.equals("3") || inputString.equals("4") || inputString.equals("5") || inputString.equals("6");
            }

            case "No" -> {
                return true;
            }

            case "Status" -> {
                return inputString.equals("booked") || inputString.equals("paid") || inputString.equals("canceled");
            }

            case "PerformanceStatus" -> {
                return inputString.equals("finished") || inputString.equals("upcoming");
            }

            case "Email" -> {
                return inputString.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
            }

            case "Passport" -> {
                return inputString.matches("\\d{4} \\d{6}");
            }

            case "Password" -> {
                return inputString.matches("\\d{4} \\d{6}");
            }
        }
        return false;
    }


}
