//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `gradle-plugin-conventions`
    alias(deps.plugins.shadowPlugin) apply false
    `java-test-fixtures`
}

gradlePlugin {
    website.set("https://github.com/SurpSG/delta-coverage")
    vcsUrl.set("https://github.com/SurpSG/delta-coverage.git")

    plugins {
        create("deltaCoveragePlugin") {
            id = "io.github.gw-kit.delta-coverage"
            displayName = "Delta Coverage"
            description = "Plugin that computes code coverage on modified code"
            implementationClass = "io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin"
            tags.set(listOf("differential", "diff", "delta", "coverage", "jacoco"))
        }
    }
}

repositories {
    gradlePluginPortal()
}

//tasks.withType<ShadowJar> {
//    archiveClassifier.set("")
//}

dependencies {
    implementation(project(":delta-coverage-core"))
    implementation(deps.koverPlugin)

    testImplementation(gradleApi()) // required to add this dependency explicitly after applying shadowJar plugin
    testImplementation(deps.jimFs)
    testRuntimeOnly(deps.kotlinJvm)

    functionalTestImplementation(project(":delta-coverage-gradle"))
    functionalTestImplementation(testFixtures(project))
    functionalTestImplementation(deps.jgit)

    testFixturesImplementation(project(":delta-coverage-core"))
    testFixturesImplementation(deps.kotestAssertions)
    testFixturesImplementation(deps.junitApi)
    testFixturesImplementation(deps.jgit)
    testFixturesImplementation(deps.mockk)
}

kover {
    excludeInstrumentation {
        packages("org.jetbrains.kotlin.gradle") // exclude to avoid coverage collecting failure
    }
}
