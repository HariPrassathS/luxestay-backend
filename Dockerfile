# Koyeb Dockerfile for Spring Boot
FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

# Copy maven wrapper and pom
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run with production profile
ENTRYPOINT ["java", "-Xmx200m", "-Xms128m", "-jar", "app.jar", "--spring.profiles.active=prod"]
