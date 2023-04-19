plugins {
    `gradle-plugin-conventions`
    alias(deps.plugins.shadowPlugin)
}

gradlePlugin {
    website.set("https://github.com/SurpSG/delta-coverage")
    vcsUrl.set("https://github.com/SurpSG/delta-coverage.git")

    plugins {
        create("deltaCoveragePlugin") {
            id = "io.github.surpsg.delta-coverage"
            displayName = "Delta Coverage"
            description = "Plugin that computes code coverage on modified code"
            implementationClass = "io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin"
            tags.set(listOf("differential", "diff", "delta", "coverage", "jacoco", "gradle", "plugin"))
        }
    }
}
tasks.withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    archiveClassifier.set("")
}

dependencies {
    implementation(project(":jacoco-filtering-extension"))

    testImplementation(gradleApi()) // required to add this dependency explicitly after applying shadowJar plugin

    functionalTestImplementation(project(":jacoco-filtering-extension"))
    functionalTestImplementation(deps.jgit)
}
