# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only pom.xml first and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Now copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Final stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create WAL directory structure with proper permissions
# Match the exact paths from application-prod.properties
RUN mkdir -p /app/data/wal/archived && \
    touch /app/data/wal/activeWal.log && \
    chown -R spring:spring /app/data

# Switch to non-root user
USER spring:spring

# Copy jar from builder
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Environment variables with correct naming to match application properties
ENV PORT=8080
ENV WAL_ACTIVE_WAL_PATH=/app/data/wal/activeWal.log
ENV WAL_ARCHIVED_WAL_DIRECTORY_PATH=/app/data/wal/archived

# Set Java options for better container performance
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:${PORT}/actuator/health || exit 1

# Run with proper configuration
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
    -Dserver.port=${PORT} \
    -Dwal.active-wal-path=${WAL_ACTIVE_WAL_PATH} \
    -Dwal.archived-wal-directory-path=${WAL_ARCHIVED_WAL_DIRECTORY_PATH} \
    -jar app.jar"]