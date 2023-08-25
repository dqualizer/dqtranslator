import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val dqlang_version = "2.0.1-SNAPSHOT"

plugins {
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"

	kotlin("jvm") version "1.9.0"
	kotlin("plugin.spring") version "1.9.0"
	kotlin("plugin.serialization") version "1.9.0"

	id("net.researchgate.release") version "3.0.2"
	id("maven-publish")
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

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
	implementation("io.github.dqualizer:dqlang:$dqlang_version")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.1")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
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
