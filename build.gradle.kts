plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.versions.update)
    alias(libs.plugins.testBalloon)
    id("maven-publish")
}

group = "nkiesel"
version = "1.0.3"

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(25)
    @Suppress("UnsafeCompilerArguments")
    compilerOptions {
        suppressWarnings = true
        freeCompilerArgs.set(
            listOf(
                "context-sensitive-resolution",
                "collection-literals",
                "name-based-destructuring=name-mismatch",
            ).map { "-X$it" }
        )
    }

    sourceSets {
        test {
            dependencies {
                implementation(libs.testBalloon.framework.core)
                implementation(libs.testBalloon.integration.kotest.assertions)
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}

tasks.test {
    minHeapSize = "1g"
    maxHeapSize = "40g"
    testLogging.showStandardStreams = true
    filter {
        setIncludePatterns("*Test")
    }
}
