plugins {
    kotlin("jvm") version "1.4.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

// Main module

kotlin.sourceSets["main"].kotlin.srcDirs("src")
sourceSets["main"].resources.srcDirs("resources")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

repositories {
    mavenCentral()
    jcenter()

    maven(url = "https://dl.bintray.com/hotkeytlt/maven")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("com.github.h0tk3y.betterParse", "better-parse", "0.4.2")

    implementation("com.andreapivetta.kolor", "kolor", "1.0.0")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.2")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.5.2")
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            enableLanguageFeature("NewInference")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

application {
    mainClassName = "cyan.EntrypointKt"
}
