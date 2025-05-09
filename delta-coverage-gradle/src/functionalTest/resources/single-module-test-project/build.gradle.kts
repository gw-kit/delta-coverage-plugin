import io.github.surpsg.deltacoverage.gradle.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.CoverageEntity.*

plugins {
    java
    kotlin("jvm") version "2.1.20"
    id("io.github.gw-kit.delta-coverage")
    // {EXTRA_PLUGINS_PLACEHOLDER}
}

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
