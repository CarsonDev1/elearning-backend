# Build stage
FROM maven:3.9.5-openjdk-17 AS build

WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-alpine

WORKDIR /app

# Install curl for health check
RUN apk add --no-cache curl

# Copy the built JAR from build stage
COPY --from=build /app/target/japanese-learning-platform-0.0.1-SNAPSHOT.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Set JVM options
ENV JAVA_OPTS="-Xmx1024m -Xms512m"

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8082/api/actuator/health || exit 1

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]
