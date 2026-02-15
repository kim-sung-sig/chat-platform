plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "com.example.chat"
version = "0.0.1-SNAPSHOT"
description = "chat-system-server"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val snippetsDir by extra { file("build/generated-snippets") }

dependencies {
    implementation(project(":common:core"))
    implementation(project(":common:security"))
    implementation(project(":common:web"))
    implementation(project(":common:logging"))

    implementation(project(":apps:chat:libs:chat-domain"))
    implementation(project(":apps:chat:libs:chat-storage"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("org.springframework.security:spring-security-test")

    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("com.h2database:h2")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.springframework.boot:spring-boot-starter-batch")
    testImplementation("org.springframework.batch:spring-batch-test")
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    testImplementation("org.springframework.amqp:spring-rabbit-test")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("com.redis.testcontainers:testcontainers-redis-junit:1.6.4")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    outputs.dir(snippetsDir)
}

tasks.named("asciidoctor") {
    inputs.dir(snippetsDir)
    dependsOn("test")
}

