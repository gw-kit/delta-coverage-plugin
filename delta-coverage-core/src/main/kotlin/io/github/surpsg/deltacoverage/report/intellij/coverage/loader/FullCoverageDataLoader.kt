package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.report.data.BinaryReport
import io.github.surpsg.deltacoverage.report.intellij.coverage.IntellijSourceInputs

internal class FullCoverageDataLoader {

    fun load(
        binaryReports: List<BinaryReport>,
        intellijSourceInputs: IntellijSourceInputs,
    ): ProjectData {
        val loadStrategy = ReportLoadStrategy.RawReportLoadStrategy(
            binaryReports,
            intellijSourceInputs.classesFiles,
            intellijSourceInputs.sourcesFiles,
            Filters(
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
            )
        )

        return filterProjectDataByExcludePatterns(
            intellijSourceInputs,
            loadStrategy.projectData,
            intellijSourceInputs.excludeClasses,
        )
    }

    private fun filterProjectDataByExcludePatterns(
        intellijSourceInputs: IntellijSourceInputs,
        sourceProjectData: ProjectData,
        excludePatterns: Set<String>
    ): ProjectData {
        val filteredData = ProjectData().apply { setInstructionsCoverage(true) }
        val classesDirLoader = ClassesDirLoader(excludePatterns)
        intellijSourceInputs.classesFiles.asSequence()
            .flatMap(classesDirLoader::traverseClasses)
            .map { jvmClassToKeep ->
                ClassDataCopingContext(
                    jvmClassToKeep.className,
                    sourceProjectData,
                    filteredData,
                )
            }
            .forEach(ClassDataCopingContext::copyClassData)
        return filteredData
    }
}
