import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.9.0"
	kotlin("plugin.spring") version "1.9.0"
	kotlin("plugin.serialization") version "1.9.0"

	id("net.researchgate.release") version "3.0.2"
	id("maven-publish")

}

group = "de.dqualizer"
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
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
	implementation("io.github.dqualizer:dqlang:2.0.16")
	implementation("io.ktor:ktor-client-core:2.3.3")
	implementation("io.ktor:ktor-client-cio:2.3.3")
	implementation("io.ktor:ktor-client-logging:2.3.3")

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
