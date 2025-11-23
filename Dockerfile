FROM eclipse-temurin:21-jre

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY build/libs/DPlay-Server-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/application-prod.yml application-prod.yml

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar", "--spring.profiles.active=prod", "--spring.config.location=file:./application-prod.yml"]