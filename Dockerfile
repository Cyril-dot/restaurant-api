# Use Ubuntu as base image
FROM ubuntu:latest
LABEL authors="lenovo"

# Install OpenJDK 21 and Maven
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk maven curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching)
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Copy source code
COPY src ./src

# Build the Spring Boot project
RUN ./mvnw clean package -DskipTests

# Expose port from Render environment variable
EXPOSE 8443

# Set environment variables for Railway MySQL
ENV DB_HOST=mysql.railway.internal
ENV DB_PORT=3306
ENV DB_NAME=railway
ENV DB_USERNAME=root
ENV DB_PASSWORD=mEPWHVfMCNeSalHXYAwkbiDMgKQdjdxc

# Start the Spring Boot app using Render's PORT variable
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar target/Restaurant_Application-0.0.1-SNAPSHOT.jar"]
