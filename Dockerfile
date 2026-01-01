# ===== 1) Build stage =====
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Gradle wrapper & config first (better cache)
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy sources and build
COPY . .
RUN ./gradlew -x test clean bootJar --no-daemon

# ===== 2) Runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy built jar (name may vary)
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Safe JVM options for small instances (t2.micro)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:InitialRAMPercentage=20 -XX:+UseContainerSupport -Duser.timezone=Asia/Seoul"

EXPOSE 8080

# NOTE: application-prod.yml is NOT copied into the image.
# It should be mounted at runtime: /app/application-prod.yml
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar --spring.profiles.active=prod --spring.config.location=file:/app/application-prod.yml"]