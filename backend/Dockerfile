# Use Maven with JDK for build and run
FROM maven:3.9-eclipse-temurin-17-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy project files
COPY pom.xml .
COPY src ./src

# Expose port
EXPOSE 8080

# For dev: run with spring-boot-devtools
# Maven will download dependencies on first run
CMD ["mvn", "spring-boot:run"]
