# Execution environment (local, ci)
ARG EXECUTION_ENV=local

# User used in local builds to access github packages
ARG GITHUB_USER

# Token used in local builds to access github packages
# or GITHUB_TOKEN in CI
ARG GITHUB_TOKEN


FROM --platform=$BUILDPLATFORM gradle:8-alpine AS builder-base-image

# Set the working directory to /app
WORKDIR /app
COPY . .


### ----------- Builder CI ----------- ###
FROM builder-base-image as ci-builder
ARG GITHUB_TOKEN
ENV GITHUB_TOKEN=$GITHUB_TOKEN


### ----------- Builder LOCAL ----------- ###
FROM builder-base-image as local-builder
ARG GITHUB_USER
ENV GITHUB_USER=$GITHUB_USER
ARG GITHUB_TOKEN
ENV GITHUB_TOKEN=$GITHUB_TOKEN

# ensure envsubst
RUN apk update && apk add gettext

### ----------- Builder Resolver and Executor ----------- ###
FROM ${EXECUTION_ENV}-builder as build-executor
RUN gradle -PgprPassword=$GITHUB_TOKEN -PgprUsername=$GITHUB_USER assemble --no-daemon

#### ----------- Runner Definiton ----------- ###
FROM --platform=$BUILDPLATFORM eclipse-temurin:19-jre-alpine

# Set the working directory to /app
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build-executor /app/build/libs/*.jar /app/dqtranslator.jar

RUN wget -O ./opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.26.0/opentelemetry-javaagent.jar


EXPOSE 8080

# Run the jar file
CMD ["java","-javaagent:opentelemetry-javaagent.jar", "-jar", "dqtranslator.jar"]
