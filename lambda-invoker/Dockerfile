# === STAGE 1: Build the Application ===
# Use an official Maven image that includes JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# 1. Copy *only* the pom.xml
COPY pom.xml .

# 2. Download dependencies into their own layer
# This step is only re-run if pom.xml changes
RUN mvn -q dependency:go-offline

# 3. Copy your source code
COPY src ./src

# 4. Build the application (this will use the cached dependencies)
# This is the line that was failing
RUN mvn -q package

# === STAGE 2: Create the Final, Small Image ===
# Use the JRE image you already have
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy *only* the final .jar file from the 'builder' stage
# Make sure to update the name of the jar file to match your project
COPY --from=builder /workspace/target/lambda-invoker-*.jar /app/app.jar

# Set the command to run your application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
