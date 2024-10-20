plugins {
    `basic-subproject-conventions`
    `maven-publish`
}

dependencies {
    implementation(deps.jgit)
    implementation(deps.intellijCoverage)
    implementation(deps.jacocoCore)
    implementation(deps.jacocoReport)
    implementation(deps.httpClient)
    implementation(deps.openCsv)
}

publishing {
    publications {
        create<MavenPublication>("deltaCoverageCorePublishing") {
            from(components["java"])
        }
    }
}
