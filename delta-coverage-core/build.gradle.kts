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
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")

    testImplementation(deps.jimFs)
}
