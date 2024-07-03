FROM gradle:8 AS builder
ARG GITHUB_USER
ARG GITHUB_TOKEN

WORKDIR /app
COPY . .

RUN gradle -PgprPassword=$GITHUB_TOKEN -PgprUsername=$GITHUB_USER assemble --no-daemon

FROM eclipse-temurin:21-jre-alpine AS rt

WORKDIR /app

RUN wget -O ./opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
COPY --from=builder /app/build/libs/*.jar /app/dqtranslator.jar

EXPOSE 8080

HEALTHCHECK --interval=25s --timeout=3s --retries=2 CMD wget --spider http://localhost:8080/actuator/health || exit 1

CMD ["java","-javaagent:opentelemetry-javaagent.jar", "-jar", "dqtranslator.jar"]