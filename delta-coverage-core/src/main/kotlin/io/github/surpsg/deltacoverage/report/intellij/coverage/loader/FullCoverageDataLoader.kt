package io.github.surpsg.deltacoverage.report.intellij.coverage.loader

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.report.data.BinaryReport
import io.github.surpsg.deltacoverage.report.intellij.coverage.IntellijSourceInputs
import java.nio.file.FileSystems

internal class FullCoverageDataLoader {

    fun load(
        binaryReports: List<BinaryReport>,
        intellijSourceInputs: IntellijSourceInputs,
    ): ProjectData {
        val loadStrategy = ReportLoadStrategy.RawReportLoadStrategy(
            binaryReports,
            intellijSourceInputs.classesRoots,
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
        )
    }

    private fun filterProjectDataByExcludePatterns(
        intellijSourceInputs: IntellijSourceInputs,
        sourceProjectData: ProjectData,
    ): ProjectData {
        val filteredData = ProjectData().apply { setInstructionsCoverage(true) }
        val classesDirLoader = ClassesDirLoader(
            intellijSourceInputs.allClasses,
            intellijSourceInputs.excludeClasses,
        )
        intellijSourceInputs.classesRoots.asSequence()
            .map { it.toPath() }
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
