import io.gradle.surpsg.deltacoverage.testkit.IntellijCoverageTestKitExtension

plugins {
    id("basic-subproject-conventions")

    id("java-gradle-plugin")
    id("functional-tests-conventions")
    id("exps-koverage")
    `java-test-fixtures`

    id("com.gradle.plugin-publish")
}

val functionalTestTaskName = "functionalTest"
val functionalTestSuite: JvmTestSuite = testing.suites.getByName(functionalTestTaskName) as JvmTestSuite
configure<GradlePluginDevelopmentExtension> {
    testSourceSets(
        functionalTestSuite.sources,
        project.extensions.getByType(JavaPluginExtension::class).sourceSets.getByName("testFixtures")
    )
}

configure<IntellijCoverageTestKitExtension> {
    testTaskName = functionalTestTaskName
}
