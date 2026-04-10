# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy parent pom
COPY pom.xml .
COPY services/user-service ./services/user-service

# Build
RUN mvn clean package -DskipTests -f services/user-service/pom.xml

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/services/user-service/target/user-service-*.jar app.jar

# Expose port
EXPOSE 8001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8001/api/users/health || exit 1

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
