plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.2.0"
  kotlin("plugin.spring") version "2.2.21"
  kotlin("plugin.jpa") version "2.2.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("commons-io:commons-io:2.21.0")
  implementation("com.googlecode.libphonenumber:libphonenumber:9.0.20")
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.4.2")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.6.3")
  implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.28.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.8")
  testImplementation("com.h2database:h2:2.4.240")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
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
