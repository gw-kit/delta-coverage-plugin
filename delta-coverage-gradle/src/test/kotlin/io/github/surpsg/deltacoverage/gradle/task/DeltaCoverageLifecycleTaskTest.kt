package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.matchers.shouldBe
import org.gradle.api.internal.project.ProjectInternal
import org.junit.jupiter.api.Test
import java.io.File

class DeltaCoverageLifecycleTaskTest {

    @Test
    fun `should build empty json array when no coverage summaries`() {
        // GIVEN
        val project: ProjectInternal = testJavaProject {
            applyDeltaCoveragePlugin()
        }
        val output: File = project.layout.buildDirectory.file("aggregated-summary.json").get().asFile.apply {
            parentFile.mkdirs()
        }

        val deltaTask: DeltaCoverageLifecycleTask = project.tasks.withType(DeltaCoverageLifecycleTask::class.java) {
            it.summaries.setFrom(emptyList<Any>())
            it.aggregatedSummary.set(output)
        }.first()

        // WHEN
        deltaTask.executeAction()

        // THEN
        output.readText() shouldBe "[]"
    }
}
