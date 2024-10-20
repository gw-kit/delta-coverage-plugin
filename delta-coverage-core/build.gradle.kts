plugins {
    `basic-subproject-conventions`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("deltaCoverageCore") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation(deps.jgit)
    implementation(deps.intellijCoverage)
    implementation(deps.jacocoCore)
    implementation(deps.jacocoReport)
    implementation(deps.httpClient)
    implementation(deps.openCsv)
}
