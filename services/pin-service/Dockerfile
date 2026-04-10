# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy parent pom and module
COPY pom.xml .
COPY services/pin-service ./services/pin-service

# Build only pin-service
RUN mvn clean package -DskipTests -f services/pin-service/pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/services/pin-service/target/pin-service-*.jar app.jar

# Expose service port
EXPOSE 8002

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8002/api/pins/health || exit 1

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
