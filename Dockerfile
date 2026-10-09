
# Use a lightweight Java runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the JAR built by Jenkins
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
