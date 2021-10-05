import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot") version "2.5.5"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version "1.5.31"
  kotlin("plugin.spring") version "1.5.31"
}

group = "kevinhwang.gpmailcontactssync"
version = "0.0.1-SNAPSHOT"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
  maven { url = uri("https://repo.spring.io/release") }
}

dependencyManagement {
  imports {
    mavenBom("com.google.cloud:libraries-bom:23.1.0")
  }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.google.api-client:google-api-client")
  implementation("com.google.auth:google-auth-library-oauth2-http")
  implementation("com.google.oauth-client:google-oauth-client-jetty:1.32.1")
  implementation("com.google.apis:google-api-services-people:v1-rev20210903-1.32.1")
  implementation("io.github.microutils:kotlin-logging:2.0.11")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  developmentOnly("io.projectreactor:reactor-tools")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("com.google.truth:truth:1.1.3")
}

java {
  sourceCompatibility = JavaVersion.VERSION_16
}

val copyDependencies = task<Copy>("copyDependencies") {
  from(configurations.runtimeClasspath)
  into("${buildDir}/libs/dependencies")
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "16"
    }
  }
  getByName<Jar>("jar") {
    enabled = false
  }
  withType<BootJar> {
    launchScript()
    dependsOn += copyDependencies
  }
  withType<Test> {
    useJUnitPlatform()
  }
}
