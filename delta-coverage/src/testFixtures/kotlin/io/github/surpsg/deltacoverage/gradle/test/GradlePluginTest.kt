package io.github.surpsg.deltacoverage.gradle.test

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Prepares a test class for running a Gradle plugin test.
 * The extension finds the test project in resources and copies it to a temporary directory.
 *
 * @param resourceProjectDir The relative path to the test project located in resources.
 * @param kts copies only `build.gradle.kts` files from resources project if any, otherwise copies `build.gradle` files.
 *
 * @see GradlePluginTestExtension
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(GradlePluginTestExtension::class)
annotation class GradlePluginTest(
    val resourceProjectDir: String,
    val kts: Boolean = false,
)

/**
 * Injects a GradleRunner instance into test class property.
 * The property must be of type GradleRunner and must be lateinit.
 * ```
 * @GradlePluginTest("testProject")
 * class MyTest {
 *
 *   @GradleRunnerInstance
 *   lateinit var gradleRunner: GradleRunner
 *
 * }
 * ```
 */
annotation class GradleRunnerInstance

/**
 * Injects the root project directory into test class property.
 * The property must be of type [java.io.File] and must be lateinit.
 * ```
 * @GradlePluginTest("testProject")
 * class MyTest {
 *
 *  @RootProjectDir
 *  lateinit var rootProjectDir: File
 *
 * }
 * ```
 */
annotation class RootProjectDir

/**
 * Resolves and injects a project file into test class property. The file must exist.
 * The property must be of type [java.io.File] or [String] and must be lateinit.
 * ```
 * @GradlePluginTest("testProject")
 * class MyTest {
 *
 *  @ProjectFile("build.gradle.kts")
 *  lateinit var buildFile: File
 *
 *  @ProjectFile("build.gradle.kts")
 *  lateinit var buildFilePath: String
 *
 *  }
 *  ```
 * @param relativePath The relative path to the project file.
 * Useful when the test class has `TestInstance.Lifecycle.PER_CLASS` lifecycle.
 */
annotation class ProjectFile(
    val relativePath: String,
)
