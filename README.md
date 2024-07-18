# dqtranslator

## Description

The dqtranslator is a crucial component responsible for translating
domain-centric RQA (Runtime Quality Analysis) Definitions into technical RQA Configurations that can be executed
by [dqexec](https://github.com/dqualizer/dqexec).
After execution, it also translates the technical results produced by dqexec back into domain-level language, making
them comprehensible to domain experts.

Think of the dqtranslator as a bridge among the other components, converting technical analysis configurations and
results into terms easily comprehensible by non-technical users (aka domain experts).

A more detailed description of this component's architecture is provided in
the [arc42 document](https://dqualizer.github.io/dqualizer).

## Development

### Prerequisites

1.<b> Java Development Kit (JDK) 17 or higher: </b> Ensure that a JDK is installed on your machine as it's required to
build and run Java applications.

2.<b> Gradle: </b> Even though we provide the Gradle wrapper, a local installation of Gradle is advantageous for running
the application with your IDE. We currently use Gradle 8.5.0.

3.<b> IDE Recommendation: </b> Since Spring Boot applications like dqtranslator are easily developed with an Integrated
Development Environment (IDE), we recommend IntelliJ IDEA for its robust support for Spring Boot and Gradle.

4.<b> Docker: </b> Install Docker on your local machine to work with containers. You might want to deploy the
dqTranslator application locally.

5.<b> GitHub Token </b>: You need to create a personal GitHub Access Token, so you can retrieve the GitHub Package of
dqlang. [Managing GitHub Tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)

Please refer to the respective official documentation for detailed installation and configuration instructions. This
section is just a guide to get you started with the essential prerequisites for setting up and deploying dqTranslator.

### Compiling

Before you can start developing, you have to set up your development environment properly.
If it doesnt exist, create a gradle.properties in your GRADLE_HOME directory with the following content:

```
gprUsername=YOUR_GITHUB_USERNAME
gprPassword=YOUR_GITHUB_ACCESS_TOKEN
```

[Here](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)
you can look up, how to create a GitHub Access Token.
Without this configuration, you are not able to access dqlang or push a dqtranslator container to the registry.

### Building

It is considered best prectice to use the projects wrapper to execute gradle tasks like building and running the
application.
So consider to use
```./gradlew build``` or ```./gradlew run```
You could also use the locally installed gradle instance by using ```gradle build / run / assemble ...``` or your IDE
Shortcuts.

### Building a local Docker Image

Depending on your operating system you can use the dockerfiles in /docker.
Use /docker/ubuntu/Dockerfile to build on M1 / M2 Chips and /docker/alpine/Dockerfile for all other platforms.
Example:
```docker buildx build -t dqtranslator:no-alpine -f deployment/docker/ubuntu/Dockerfile --build-arg="GITHUB_USER=SomeUser" --build-arg="GITHUB_TOKEN=SomeToken" .```
