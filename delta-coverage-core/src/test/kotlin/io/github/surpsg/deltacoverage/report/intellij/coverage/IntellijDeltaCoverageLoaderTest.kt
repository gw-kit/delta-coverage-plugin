package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.kotest.matchers.maps.shouldBeEmpty
import org.junit.jupiter.api.Test

internal class IntellijDeltaCoverageLoaderTest {

    @Test
    fun `getDeltaProjectData should return empty data if binary reports list is empty`() {
        // WHEN
        val deltaProjectData: ProjectData = IntellijDeltaCoverageLoader.getDeltaProjectData(
            emptyList(),
            IntellijSourceInputs(emptyList(), emptyList()),
            CodeUpdateInfo(emptyMap())
        )

        // THEN
        deltaProjectData.classes.shouldBeEmpty()
    }
}
