plugins {
    `basic-subproject-conventions`
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
    implementation(deps.intellijCoverage)
    implementation(deps.jacocoCore)
    implementation(deps.jacocoReport)
    implementation(deps.httpClient)
    implementation(deps.openCsv)
    implementation(deps.jackson)

    testImplementation(deps.jimFs)
}
