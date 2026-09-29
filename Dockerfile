# Use lightweight JDK image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy jar from target
COPY target/*.jar app.jar

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]