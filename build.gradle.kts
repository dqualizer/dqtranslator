val dqlangVersion = "3.1.14-SNAPSHOT"

plugins {
  kotlin("jvm") version "2.0.0"
  kotlin("plugin.spring") version "2.0.0"
  kotlin("plugin.serialization") version "2.0.0"

  id("org.springframework.boot") version "3.2.5"
  id("io.spring.dependency-management") version "1.1.5"

  id("net.researchgate.release") version "3.0.2"
  id("maven-publish")
  id("idea")
  id("eclipse")
}

group = "io.github.dqualizer"

release {
  //no config needed, see https://github.com/researchgate/gradle-release for options
}

publishing {
  repositories {
    maven {
      name = "gpr"
      url = uri("https://maven.pkg.github.com/dqualizer/dqtranslator")
      credentials(PasswordCredentials::class)
    }
    publications {
      register("jar", MavenPublication::class) {
        from(components["java"])
        pom {
          url.set("https://github.com/dqualizer/dqtranslator.git")
        }
      }
    }
  }
}


configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
  maven {
    name = "gpr"
    url = uri("https://maven.pkg.github.com/dqualizer/dqlang")
    credentials(PasswordCredentials::class)
  }
}

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.5"))

  implementation("org.springframework.boot:spring-boot-starter-amqp")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
  implementation("org.springframework.boot:spring-boot-starter-web")

  implementation("io.ktor:ktor-client-core:2.3.10")
  implementation("io.ktor:ktor-client-cio:2.3.10")
  implementation("io.ktor:ktor-client-logging:2.3.10")

  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

  implementation("io.github.dqualizer:dqlang:$dqlangVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.amqp:spring-rabbit-test")
  testImplementation("org.jeasy:easy-random-core:5.0.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
  //assertJ
  testImplementation("org.assertj:assertj-core:3.25.3")
  testImplementation("com.github.fridujo:rabbitmq-mock:1.2.0")

  //testcontainers
  testImplementation("org.testcontainers:testcontainers")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:rabbitmq")
  testImplementation("org.testcontainers:mongodb")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}


tasks.withType<Test> {
  useJUnitPlatform()
}

// Disable generation of "-plain" jar by the Spring Boot plugin
tasks.getByName<Jar>("jar") {
  enabled = false
}
