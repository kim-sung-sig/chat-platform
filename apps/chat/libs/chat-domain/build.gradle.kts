plugins {
    kotlin("jvm")
}

group = "com.example.chat"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(emptyList<String>())
        }
        kotlin {
            setSrcDirs(listOf("src/main/kotlin"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common:core"))

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.mockk:mockk:1.13.9")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

