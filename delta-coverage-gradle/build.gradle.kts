import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `gradle-plugin-conventions`
    alias(deps.plugins.shadowPlugin)
    `java-test-fixtures`
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
            tags.set(listOf("differential", "diff", "delta", "coverage", "jacoco"))
        }
    }
}
tasks.withType(ShadowJar::class.java) {
    archiveClassifier.set("")
}

repositories {
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.3")

    testImplementation(gradleApi()) // required to add this dependency explicitly after applying shadowJar plugin
    testImplementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    testImplementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.3")
    testImplementation("com.google.jimfs:jimfs:1.3.0")

    functionalTestImplementation(project(":delta-coverage-gradle"))
    functionalTestImplementation(deps.jgit)
    functionalTestImplementation(testFixtures(project))

    testFixturesImplementation(deps.assertj)
    testFixturesImplementation(deps.junitApi)
    testFixturesImplementation(deps.jgit)
    testFixturesImplementation(project(":delta-coverage-core"))
}
