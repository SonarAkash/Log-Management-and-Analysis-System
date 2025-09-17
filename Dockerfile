# === Stage 1: The Builder ===
# An official Maven image to build the application JAR file.
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the project's pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, creating the executable JAR. Skip tests.
RUN mvn clean package -DskipTests

# === Stage 2: The Final Image ===
# Use a lightweight Java image for the final container.
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

RUN apt-get update && apt-get install -y curl

# Copy only the built JAR from the 'builder' stage into this final image
COPY --from=builder /app/target/*.jar app.jar

# Create directory for WAL files
RUN mkdir -p /app/data/wal/active /app/data/wal/archived

# Expose the port the application runs on
COPY entrypoint.sh .
EXPOSE 8080

ENTRYPOINT ["./entrypoint.sh"]