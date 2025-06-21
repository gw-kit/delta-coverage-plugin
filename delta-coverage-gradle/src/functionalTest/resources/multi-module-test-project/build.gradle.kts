import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.CoverageEntity.*
import io.github.surpsg.deltacoverage.gradle.ReportView
import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    java
    kotlin("jvm") version "2.1.20"
    id("io.github.gw-kit.delta-coverage")
    `java-test-fixtures`
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.13.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    testing.suites {
        val intTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(platform("org.junit:junit-bom:5.13.1"))
                implementation("org.junit.jupiter:junit-jupiter")
            }
        }
    }
}

