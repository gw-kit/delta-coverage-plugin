package io.github.surpsg.deltacoverage.gradle.test

import io.github.surpsg.deltacoverage.gradle.buildGradleRunner
import io.github.surpsg.deltacoverage.gradle.resources.copyDirFromResources
import io.github.surpsg.deltacoverage.gradle.resources.toUnixAbsolutePath
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import java.io.File
import java.nio.file.Files
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class GradlePluginTestExtension : TestInstancePostProcessor {

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val testClass: KClass<out Any> = testInstance::class

        if (testClass.hasAnnotation<Nested>()) {
            return
        }

        val gradlePluginTest: GradlePluginTest = testClass.findAnnotation()
            ?: error("Test class ${testInstance::class.qualifiedName} must be annotated with @GradlePluginTest")

        val rootProjectDir: File = copyTestProjectFromResources(gradlePluginTest)
        val tempTestFile: File = Files.createTempDirectory(TEST_DIR_PREFIX).toFile()

        with(testInstance) {
            injectProperty<RootProjectDir, File>(rootProjectDir)
            injectProperty<GradleRunnerInstance, GradleRunner>(buildGradleRunner(rootProjectDir))
            injectProperty<ProjectFile, RestorableFile> {
                val fileToBeRestored: File = resolveExistingFile(rootProjectDir, relativePath)
                val originCopy: File = tempTestFile.resolve(UUID.randomUUID().toString())
                fileToBeRestored.copyTo(originCopy)
                RestorableFile(originFileCopy = originCopy, file = fileToBeRestored)
            }
            injectProperty<ProjectFile, File> {
                resolveExistingFile(rootProjectDir, relativePath)
            }
            injectProperty<ProjectFile, String> {
                resolveExistingFile(rootProjectDir, relativePath).toUnixAbsolutePath()
            }
        }
    }

    private fun copyTestProjectFromResources(gradlePluginTest: GradlePluginTest): File {
        val tempTestFile = Files.createTempDirectory(TEST_DIR_PREFIX).toFile()

        val rootProjectDir = tempTestFile.copyDirFromResources<GradlePluginTestExtension>(
            gradlePluginTest.resourceProjectDir
        )
        processBuildFiles(rootProjectDir, gradlePluginTest)

        return rootProjectDir
    }

    private fun processBuildFiles(
        rootProjectDir: File,
        gradlePluginTest: GradlePluginTest,
    ) {
        val buildFileNameToFile: Map<String, List<File>> = rootProjectDir.walkTopDown()
            .filter { GRADLE_BUILD_FILES.contains(it.name) }
            .fold(mutableMapOf<String, MutableList<File>>()) { acc, file ->
                acc.getOrPut(file.name) { mutableListOf() }.add(file)
                acc
            }

        val buildFileToDelete: String = if (gradlePluginTest.kts) GROOVY_BUILD_FILE_NAME else KOTLIN_BUILD_FILE_NAME
        buildFileNameToFile[buildFileToDelete]?.forEach {
            it.delete()
        }
    }

    private fun resolveExistingFile(rootProjectDir: File, relativePath: String): File {
        return rootProjectDir.resolve(relativePath)
            .takeIf { it.exists() }
            ?: error("File $relativePath not found")
    }

    private inline fun <reified A : Annotation, reified V> Any.injectProperty(
        valueToInject: V,
    ) {
        val thisInstance = this
        thisInstance::class
            .memberProperties
            .asSequence()
            .filter { property -> property.hasAnnotation<A>() }
            .filter { property -> property.returnType.classifier == V::class }
            .filter { property -> property.isLateinit }
            .onEach { property -> property.isAccessible = true }
            .mapNotNull { property -> property as? KMutableProperty<*> }
            .forEach { property -> property.setter.call(thisInstance, valueToInject) }
    }

    private inline fun <reified A : Annotation, reified V> Any.injectProperty(
        crossinline valueProvider: A.() -> V,
    ) {
        val thisInstance = this
        thisInstance::class
            .memberProperties
            .asSequence()
            .mapNotNull { property ->
                property.findAnnotation<A>()?.let { annotation -> annotation to property }
            }
            .filter { (_, property) -> property.returnType.classifier == V::class }
            .filter { (_, property) -> property.isLateinit }
            .onEach { (_, property) -> property.isAccessible = true }
            .mapNotNull { (annotation, property) ->
                (property as? KMutableProperty<*>)?.let { mutableProperty ->
                    annotation.valueProvider() to mutableProperty
                }
            }
            .forEach { (valueToInject, property) -> property.setter.call(thisInstance, valueToInject) }
    }

    private companion object {
        const val TEST_DIR_PREFIX = "gradle-plugin-test"

        const val GROOVY_BUILD_FILE_NAME = "build.gradle"
        const val KOTLIN_BUILD_FILE_NAME = "build.gradle.kts"

        val GRADLE_BUILD_FILES = setOf(GROOVY_BUILD_FILE_NAME, KOTLIN_BUILD_FILE_NAME)
    }
}
