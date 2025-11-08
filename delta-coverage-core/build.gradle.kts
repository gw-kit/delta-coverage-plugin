plugins {
    `jvm-project-conventions`
    `java-library`
    alias(deps.plugins.mavenPublish)
}

dependencies {
    implementation(deps.jgit)
    implementation(deps.intellijCoverage)
    implementation(deps.jacocoCore)
    implementation(deps.jacocoReport)
    implementation(deps.httpClient)
    implementation(deps.openCsv)
    implementation(deps.jackson)

    testImplementation(deps.jimFs)
}

configurations.all {
    resolutionStrategy.force(deps.intellijCoverageAgent)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Only sign publications for Maven Central, not for GitHub Packages snapshots
    if (!rootProject.hasProperty("snapshotPrefix")) {
        signAllPublications()
    }
}
