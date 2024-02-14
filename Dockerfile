FROM gradle:8.6.0-jdk21 AS build

WORKDIR /app

COPY src /app/src
COPY build.gradle.kts settings.gradle.kts /app/

RUN gradle clean build

FROM openjdk:21-jdk-slim AS run

RUN adduser --system --group app-user

COPY --from=build --chown=app-user:app-user /app/build/libs/github-api-*.jar github-api.jar

EXPOSE 8080
USER app-user

CMD ["java", "-jar", "github-api.jar"]

