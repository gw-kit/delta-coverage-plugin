package io.github.gwkit.testimpact.gradle

import io.github.gwkit.testimpact.gradle.config.TestImpactConfiguration
import io.github.gwkit.testimpact.gradle.task.GenerateJfcConfigTask
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.string.shouldContain
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.junit.jupiter.api.Test
import org.gradle.api.tasks.testing.Test as TestTask

class TestImpactPluginTest {

    @Test
    fun `apply plugin should configure test task with JFR argument provider when enabled`() {
        // GIVEN // WHEN
        val project = testJavaProject {
            pluginManager.apply(TestImpactPlugin::class.java)
            extensions.configure(TestImpactConfiguration::class.java) { config ->
                config.enabled.set(true)
            }
            tasks.named("generateJfcConfig", GenerateJfcConfigTask::class.java) {
                it.state.outcome = TaskExecutionOutcome.EXECUTED
            }
        }

        // THEN
        assertSoftly(project.tasks.withType(TestTask::class.java).getByName("test")) {
            jvmArgumentProviders.single().asArguments().single() shouldContain Regex(
                "-XX:StartFlightRecording=filename=.*,settings=.*,dumponexit=true"
            )
            outputs.files.map { it.name } shouldContainAll listOf(
                "${name}-test-events.txt",
                "${name}-recording.jfr",
            )
        }
    }
}
