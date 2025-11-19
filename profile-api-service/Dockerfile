# === STAGE 1: Build ===
# Use a standard Maven/JDK 17 image
FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /app

# 1. Copy *only* the pom.xml
COPY pom.xml .

# 2. Download all dependencies (this layer is cached)
# This step is only re-run if pom.xml changes
RUN mvn -q dependency:go-offline

# 3. Copy your source code
# Changing code will only invalidate this layer and the next
COPY src ./src

# 4. Build the package (using cached dependencies)
# Skips tests, which avoids the need for the Docker socket
RUN mvn -q package -DskipTests

# === STAGE 2: Runtime ===
# Use a lightweight JRE-only image for a smaller footprint
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built .jar file from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]