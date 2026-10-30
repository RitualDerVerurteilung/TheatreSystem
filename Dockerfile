# Сборка Maven и Java 26
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /backend

# Копирование pom.xml из контекста сборки в образ
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копирование исходного кода в образ
COPY src ./src
# Сборка приложения
RUN mvn clean package -DskipTests -B

# Запуск
FROM eclipse-temurin:26-jre
WORKDIR /backend

COPY --from=build /backend/target/TheatreSystem-1.2.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]