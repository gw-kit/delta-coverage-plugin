import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.CoverageEntity.*
import io.github.surpsg.deltacoverage.gradle.ReportView
import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    java
    kotlin("jvm") version "1.9.25"
    id("io.github.gw-kit.delta-coverage")
    `java-test-fixtures`
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.11.4"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    testing.suites {
        val intTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(platform("org.junit:junit-bom:5.11.4"))
                implementation("org.junit.jupiter:junit-jupiter")
            }
        }
    }
}

