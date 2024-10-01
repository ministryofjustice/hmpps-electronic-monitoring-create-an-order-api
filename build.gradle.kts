plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.5"
  kotlin("plugin.spring") version "2.0.20"
  kotlin("plugin.jpa") version "2.0.20"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("commons-io:commons-io:2.15.1")
  implementation("com.googlecode.libphonenumber:libphonenumber:8.13.45")
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.7")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.4.4")
  implementation("io.sentry:sentry-spring-boot-starter-jakarta:7.14.0")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.2")
  testImplementation("com.h2database:h2:2.3.232")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.0.7")
  testImplementation("org.wiremock:wiremock-standalone:3.9.1")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  register<Test>("unitTest") {
    filter {
      excludeTestsMatching("uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration*")
    }
  }

  register<Test>("integration") {
    filter {
      includeTestsMatching("uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration*")
    }
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
