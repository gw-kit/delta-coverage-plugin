plugins {
    id("jvm-project-conventions")
    id("java-gradle-plugin")
    id("functional-tests-conventions")
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
