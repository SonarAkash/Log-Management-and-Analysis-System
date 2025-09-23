# Stage 1: Build the Java application
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user. UID 1000 is standard.
RUN addgroup -S appgroup -g 1000 && adduser -S appuser -u 1000 -G appgroup

# Switch to the non-root user

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/LogManager-0.0.1-SNAPSHOT.jar app.jar

# #debug start -------

# # Create the debug directory
# RUN mkdir /app/static-debug

# # Unzip the static files into it
# RUN unzip -j app.jar BOOT-INF/classes/static/* -d /app/static-debug

# # --- Give the non-root user ownership of all the app files ---
# RUN chown -R appuser:appgroup /app

# # debug end -------
USER appuser

# The command to run the application
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]