FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR file
COPY target/japanese-learning-platform-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8082

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
