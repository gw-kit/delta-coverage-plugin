import org.gradle.kotlin.dsl.base
import org.gradle.kotlin.dsl.`java-gradle-plugin`
import org.gradle.kotlin.dsl.`jvm-test-suite`
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("basic-subproject-conventions")

    id("java-gradle-plugin")
    id("functional-tests-conventions")
    id("pl.droidsonroids.jacoco.testkit")

    id("com.gradle.plugin-publish")
}

val functionalTestSuite: JvmTestSuite = testing.suites.getByName("functionalTest") as JvmTestSuite
configure<GradlePluginDevelopmentExtension> {
    testSourceSet(functionalTestSuite.sources)
}

configure<pl.droidsonroids.gradle.jacoco.testkit.JacocoTestKitExtension> {
    applyTo("functionalTestRuntimeOnly", tasks.named("functionalTest"))
}
