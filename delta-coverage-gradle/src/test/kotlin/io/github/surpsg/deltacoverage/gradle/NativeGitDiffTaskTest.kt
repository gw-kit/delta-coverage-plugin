package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.task.NativeGitDiffTask
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class NativeGitDiffTaskTest {

    @Test
    fun `should throw when project has no git`() {
        // GIVEN
        val task: NativeGitDiffTask = with(testJavaProject()) {
            tasks.create("gitDiff", NativeGitDiffTask::class.java) {
                it.targetBranch.set("unknown-${System.currentTimeMillis()}")

                it.diffFile.get().asFile.apply {
                    parentFile.mkdirs()
                    createNewFile()
                }
            }
        }

        // WHEN // THEN
        shouldThrow<Exception> { task.obtainDiff() }
    }
}
