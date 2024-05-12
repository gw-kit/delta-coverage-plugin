package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

internal class SourceCodeLookupTest {

    @Test
    fun `should return source if source found in java project`() {
        // GIVEN
        val project: Project = testJavaProject()

        // WHEN
        val actualSources: FileCollection = SourceCodeLookup().lookupSourceCode(project)

        // THEN
        actualSources shouldHaveSize 1
    }

    @Test
    fun `should return empty source if project does not have sources`() {
        // GIVEN
        val project: Project = ProjectBuilder.builder().build()

        // WHEN
        val actualSources: FileCollection = SourceCodeLookup().lookupSourceCode(project)

        // THEN
        actualSources.shouldBeEmpty()
    }
}
