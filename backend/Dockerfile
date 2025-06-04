# Use Gradle with Java 21 to build the app
FROM gradle:8.7-jdk21 AS build

WORKDIR /app

# Copy build files first for better caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

# Build the jar
RUN gradle bootJar --no-daemon

# Use lightweight Java 21 runtime image to run the app
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
