# Stage 1: Build the application using Java 21 and Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Copy application source code
COPY src ./src

# Build the Spring Boot application and download dependencies
RUN mvn -B -ntp clean package -DskipTests


# Stage 2: Run the application using Java 21
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated JAR
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]