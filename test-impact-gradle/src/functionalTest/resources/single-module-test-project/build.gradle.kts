import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    kotlin("jvm") version "2.3.0"
    id("io.github.gw-kit.test-impact")
}

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
        showStandardStreams = true
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
