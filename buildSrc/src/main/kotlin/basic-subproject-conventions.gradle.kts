import io.gradle.surpsg.deltacoverage.libDeps
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    kotlin("jvm")
    `maven-publish`

    id("io.gitlab.arturbosch.detekt")
    id("unit-tests-conventions")

    `jacoco`
}

val targetJvmVersion = JavaLanguageVersion.of(8)
kotlin {
    jvmToolchain {
        languageVersion.set(targetJvmVersion)
    }
}

java {
    toolchain {
        languageVersion.set(targetJvmVersion)
    }
}

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = libDeps.versions.jacocoVer.get()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
