# 1) 빌드 스테이지: JAR 생성
FROM gradle:8.7.0-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle -q clean bootJar

# 2) 런타임 스테이지: 가벼운 JRE로 실행
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드 산출물 복사 (build/libs 안의 jar 1개 복사)
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# 기본은 local 프로필(Compose에서 override 가능)
ENV SPRING_PROFILES_ACTIVE=local

ENTRYPOINT ["java","-jar","/app/app.jar"]