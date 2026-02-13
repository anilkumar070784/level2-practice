# Build stage (optional but nice)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar from build image
COPY --from=build /app/target/telemetry-service-0.0.1-SNAPSHOT.jar app.jar

# Service port
EXPOSE 8082

# Environment for Feign URLs (adjust if you containerize others later)
ENV ASSET_SERVICE_URL=http://host.docker.internal:8081
ENV ALERT_SERVICE_URL=http://host.docker.internal:8083

ENTRYPOINT ["java", "-jar", "app.jar"]