plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "7.1.2"
  kotlin("plugin.spring") version "2.0.21"
  kotlin("plugin.jpa") version "2.0.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("commons-io:commons-io:2.18.0")
  implementation("com.googlecode.libphonenumber:libphonenumber:8.13.50")
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.2.1")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("io.awspring.cloud:spring-cloud-aws-starter-s3")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.3.1")
  implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.1.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.4")
  testImplementation("com.h2database:h2:2.3.232")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.2.1")
  testImplementation("org.wiremock:wiremock-standalone:3.9.2")
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
