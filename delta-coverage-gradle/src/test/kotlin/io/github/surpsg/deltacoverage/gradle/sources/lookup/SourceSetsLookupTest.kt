package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

internal class SourceSetsLookupTest {

    @Test
    fun `should return source sets if source sets found in java project`() {
        // GIVEN
        val project: Project = testJavaProject()

        // WHEN
        val actualSources: SourceSetsLookup.AutoDetectedSources = SourceSetsLookup().lookupSourceSets(project)

        // THEN
        assertSoftly(actualSources) {
            allSources shouldHaveSize 1
            allClasses shouldHaveSize 2
        }
    }

    @Test
    fun `should return empty source sets if project does not have sources sets`() {
        // GIVEN
        val project: Project = ProjectBuilder.builder().build()

        // WHEN
        val actualSources: SourceSetsLookup.AutoDetectedSources = SourceSetsLookup().lookupSourceSets(project)

        // THEN
        assertSoftly(actualSources) {
            allSources.shouldBeEmpty()
            allClasses.shouldBeEmpty()
        }
    }
}
