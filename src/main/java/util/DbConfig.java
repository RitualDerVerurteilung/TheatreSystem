package util;

public final class DbConfig {

    // Утилитный класс — без экземпляров
    private DbConfig() {}

    // Адрес БД: POSTGRES_DB, иначе стандарт
    public static String url() {

        // Если Docker передал готовый DB_URL
        String fullUrl = read("DB_URL");
        if (fullUrl != null) {
            return fullUrl;
        }

        String database = read("POSTGRES_DB");
        if (database == null) database = "Theatre";

        return "jdbc:postgresql://localhost:5432/" + database;
    }

    // Имя пользователя: POSTGRES_USER, иначе "postgres".
    public static String user() {
        String user = read("DB_USER");
        return user != null ? user : "postgres";
    }

    // Пароль: DB_PASSWORD, иначе "postgres".
    public static String password() {
        String password = read("DB_PASSWORD");
        return password != null ? password : "password";
    }

    private static String read(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}