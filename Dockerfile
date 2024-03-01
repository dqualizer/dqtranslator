FROM gradle:8 AS builder
ARG GITHUB_USER
ARG GITHUB_TOKEN

# Set the working directory to /app
WORKDIR /app
COPY . .

RUN gradle -PgprPassword=$GITHUB_TOKEN -PgprUsername=$GITHUB_USER assemble --no-daemon

#### ----------- Runner Definiton ----------- ###
FROM eclipse-temurin:21-jre-alpine

# Set the working directory to /app
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=builder /app/build/libs/*.jar /app/dqtranslator.jar

RUN wget -O ./opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.26.0/opentelemetry-javaagent.jar

EXPOSE 8080

# Run the jar file
CMD ["java","-javaagent:opentelemetry-javaagent.jar", "-jar", "dqtranslator.jar"]
