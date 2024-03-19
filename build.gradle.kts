import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.10"
	kotlin("plugin.spring") version "1.9.10"

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
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/dqualizer/dqtranslator")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
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
	mavenLocal()
	mavenCentral()
}

configurations.all {
	resolutionStrategy {
		cacheChangingModulesFor(0, "seconds")
	}
}

dependencies {
	implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.1"))

	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

	implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

	implementation("io.github.dqualizer:dqlang:3.0.0-merge_dam-SNAPSHOT")
//    implementation(project(":dqlang"))

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation("org.jeasy:easy-random-core:5.0.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
	//assertJ
	testImplementation("org.assertj:assertj-core:3.24.2")

	//testcontainers
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:rabbitmq")
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
