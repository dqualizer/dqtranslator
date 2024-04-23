import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10"
	kotlin("plugin.serialization") version "1.9.0"

    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"

    id("net.researchgate.release") version "3.0.2"
    id("maven-publish")
    id("idea")
    id("eclipse")
}

group = "io.github.dqualizer"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

release {
    //no config needed, see https://github.com/researchgate/gradle-release for options
}


publishing{
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
		 name="gpr"
		url = uri("https://maven.pkg.github.com/dqualizer/dqlang")
		credentials(PasswordCredentials::class)
	 }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.3"))

    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")
    implementation("io.ktor:ktor-client-logging:2.3.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    implementation("io.github.dqualizer:dqlang:3.1.5-SNAPSHOT")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.jeasy:easy-random-core:5.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
    //assertJ
    testImplementation("org.assertj:assertj-core:3.25.2")
    testImplementation("com.github.fridujo:rabbitmq-mock:1.2.0")

    //testcontainers
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.testcontainers:mongodb")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Disable generation of "-plain" jar by the Spring Boot plugin
tasks.getByName<Jar>("jar") {
    enabled = false
}
