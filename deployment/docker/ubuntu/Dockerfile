# Execution environment (local, ci)
ARG EXECUTION_ENV=local

# User used in local builds to access github packages
ARG GITHUB_USER
# Token used in local builds to access github packages
ARG GITHUB_TOKEN

FROM --platform=$BUILDPLATFORM gradle:8 AS builder-base-image

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

ARG DEBIAN_FRONTEND=noninteractive

# ensure envsubst
RUN apt-get -qq update
RUN apt-get -qq -y --no-install-recommends install gettext


### ----------- Builder Resolver and Executor ----------- ###
FROM ${EXECUTION_ENV}-builder as build-executor
RUN gradle -PgprPassword=$GITHUB_TOKEN -PgprUsername=$GITHUB_USER assemble --no-daemon

#### ----------- Runner Definiton ----------- ###
FROM --platform=$BUILDPLATFORM eclipse-temurin:19-jre

# Set the working directory to /app
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build-executor /app/build/libs/*.jar /app/dqtranslator.jar


# Run the jar file
CMD ["java", "-jar", "dqtranslator.jar"]
