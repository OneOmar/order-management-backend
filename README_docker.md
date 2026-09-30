# Docker Setup

This project is containerized using Docker and Docker Compose.

---

## Files Overview

### Dockerfile
Defines how the Spring Boot application is packaged into a Docker image.

Key steps:
- Uses Java 17 (Eclipse Temurin)
- Copies the built `.jar` file
- Runs the application

---

### docker-compose.yml
Defines and runs multi-container setup:

- **app** → Spring Boot app (port 8080)
- **db** → PostgreSQL (port 5432)
- Uses environment variables from `.env`
- Includes a volume for DB persistence

---

##  Prerequisites

- Docker
- Docker Compose
- Maven

---

## Environment Variables

Create a `.env` file:

POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
JWT_SECRET=your_base64_secret
ADMIN_EMAIL=admin@mail.com
ADMIN_PASSWORD=admin123

> Do not commit `.env`

---

## Run the Application

1. Build the jar:
   mvn clean package -DskipTests

2. Start containers:
   docker compose up --build

---

## Access

- API: http://localhost:8080
- Health: http://localhost:8080/actuator/health

---

## Persistence

Database data is stored in Docker volume:

postgres_data

---

## Stop

docker compose down

---

## Rebuild after changes

mvn clean package -DskipTests
docker compose up --build

---

## Useful Commands

docker compose ps
docker compose logs -f
docker compose exec app sh

---

## Notes

- Rebuild jar after code changes
- JWT secret must be Base64 (>= 256 bits)
