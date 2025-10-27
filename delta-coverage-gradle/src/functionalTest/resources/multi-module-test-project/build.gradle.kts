import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.CoverageEntity.*
import io.github.surpsg.deltacoverage.gradle.ReportView

plugins {
    id("io.github.gw-kit.delta-coverage")
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }
}

