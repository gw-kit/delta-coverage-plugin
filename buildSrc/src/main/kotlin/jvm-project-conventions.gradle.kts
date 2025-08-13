import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm")
    id("gh-publish-conventions")

    id("io.gitlab.arturbosch.detekt")
    id("unit-tests-conventions")

    id("basic-coverage-conventions")
}

val targetJvmVersion = JavaLanguageVersion.of(17)
kotlin {
    jvmToolchain {
        languageVersion.set(targetJvmVersion)
    }
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_1_9
    }
}

java {
    toolchain {
        languageVersion.set(targetJvmVersion)
    }
}

detekt {
    parallel = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/baseline.xml")
    buildUponDefaultConfig = true
}
