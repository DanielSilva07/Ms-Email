# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SPRING_MAIL_HOST=${SMTP_HOST} \
    SPRING_MAIL_PORT=${SMTP_PORT} \
    SPRING_MAIL_USERNAME=${SMTP_USER}\
    SPRING_MAIL_PASSWORD=${SMTP_PASS} \
    SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Expose the application port
EXPOSE 8080

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
