# === STAGE 1: Build ===
# Use an official Maven image with JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# 1. Copy *only* the pom.xml
# This layer is only invalidated if your pom.xml changes.
COPY pom.xml .

# 2. Download all dependencies (Dependency Caching)
# Docker caches this layer. It only re-runs if pom.xml changes.
RUN mvn -q dependency:go-offline

# 3. Now, copy your source code
# Changing code will only invalidate this layer and the next.
COPY src ./src

# 4. Build the application (using cached dependencies)
# This will be much faster as dependencies are already downloaded.
RUN mvn -q package

# === STAGE 2: Runtime ===
# This stage is the same as your original file
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]