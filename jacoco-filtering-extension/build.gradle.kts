plugins {
    `basic-subproject-conventions`
    jacoco
}

publishing {
    publications {
        create<MavenPublication>("jacocoFilteringExtension") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation(deps.jgit)
    implementation(deps.intellijCoverage) // TODO upgrade to latest version
    implementation(deps.jacocoCore)
    implementation(deps.jacocoReport)
    implementation(deps.httpClient)
}
