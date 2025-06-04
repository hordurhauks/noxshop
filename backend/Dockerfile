FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy build output (assuming you’re using Gradle)
COPY build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
