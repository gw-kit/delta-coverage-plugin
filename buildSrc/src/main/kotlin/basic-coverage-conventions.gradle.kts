import io.gradle.surpsg.deltacoverage.libDeps

plugins {
    kotlin("jvm")
    jacoco
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
