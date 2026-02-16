plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("jvm")
	kotlin("plugin.spring")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	enabled = true
}

tasks.named<Jar>("jar") {
	enabled = false
}

dependencies {
	// Spring Cloud Gateway
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")

	// Eureka Client (서비스 디스커버리)
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	// WebFlux (Gateway는 WebFlux 기반)
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// Actuator
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// Development
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}
