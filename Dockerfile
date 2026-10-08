# Le jar est construit avant (Jenkins stage 'Build' ou `./mvnw clean package -DskipTests`)
FROM eclipse-temurin:17-jre

WORKDIR /app

# copier le jar construit par Maven
COPY target/*.jar app.jar

# lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
