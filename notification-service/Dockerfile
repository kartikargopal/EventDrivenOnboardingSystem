# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

# 1. Copy *only* the pom.xml
# This layer is only invalidated if your pom.xml changes.
COPY pom.xml .

# 2. Download all dependencies
# Docker will cache this layer and re-use it
# as long as pom.xml hasn't changed.
RUN mvn -q dependency:go-offline

# 3. Now, copy your source code
# Changing code will only invalidate this layer and the next one.
COPY src ./src

# 4. Build the application
# This step will use the cached dependencies from step 2
# and will only re-compile your code. This is much faster.
RUN mvn -q package

# ---------- Runtime stage (AWS Lambda Java 17) ----------
# This stage is perfect, no changes needed.
FROM public.ecr.aws/lambda/java:17

# Copy jar from builder
COPY --from=builder /workspace/target/notification-service-1.0.0.jar ${LAMBDA_TASK_ROOT}/notification-service.jar

# Set handler (ClassName::methodName)
CMD ["com.service.notification.NotificationHandler::handleRequest"]