FROM openjdk:17-jdk-alpine

WORKDIR /app

# Install Maven and curl
RUN apk add --no-cache curl maven

# Copy source code
COPY . .

# Make mvnw executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Create logs directory
RUN mkdir -p /app/logs

# Set JVM options
ENV JAVA_OPTS="-Xmx1024m -Xms512m"

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8082/api/levels || exit 1

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar target/japanese-learning-platform-0.0.1-SNAPSHOT.jar"]
