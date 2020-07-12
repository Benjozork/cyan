plugins {
    kotlin("jvm") version "1.3.72"
}

group = "org.example"
version = "1.0-SNAPSHOT"

// Main module

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

repositories {
    mavenCentral()

    maven(url = "https://dl.bintray.com/hotkeytlt/maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.h0tk3y.betterParse", "better-parse", "0.4.0")
}

