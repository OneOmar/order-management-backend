# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

# copier les fichiers maven
COPY pom.xml .
COPY src ./src

# build du jar
RUN mvn clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:17-jdk

WORKDIR /app

# copier uniquement le jar depuis le builder
COPY --from=builder /build/target/*.jar app.jar

# lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]