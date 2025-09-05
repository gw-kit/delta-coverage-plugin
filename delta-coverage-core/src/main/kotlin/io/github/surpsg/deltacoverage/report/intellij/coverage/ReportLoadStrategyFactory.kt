package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.report.data.BinaryReport
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import java.io.File
import java.util.Deque
import java.util.LinkedList

internal object ReportLoadStrategyFactory {

    fun buildReportLoadStrategies(reportContext: ReportContext): Sequence<NamedReportLoadStrategy> {
        val binaryReports: List<BinaryReport> = buildBinaryReports(reportContext)
        val intellijSourceInputs = IntellijSourceInputs(
            classesFiles = reportContext.deltaCoverageConfig.classRoots.toList(),
            excludeClasses = reportContext.excludeClasses,
            sourcesFiles = reportContext.srcFiles.toList(),
        )

        val fullCoverageData: ProjectData = loadFullCoverageData(binaryReports, intellijSourceInputs)

        val filterProjectData: ProjectData = IntellijDeltaCoverageLoader.getDeltaProjectData(
            fullCoverageData,
            reportContext.codeUpdateInfo,
        )

        val deltaReportLoadStrategy = sequenceOf(
            NamedReportLoadStrategy(
                ReportBound.DELTA_REPORT,
                PreloadedCoverageReportLoadStrategy(filterProjectData, binaryReports, intellijSourceInputs),
            )
        )
        return if (reportContext.deltaCoverageConfig.reportsConfig.fullCoverageReport) {
            deltaReportLoadStrategy + NamedReportLoadStrategy(
                ReportBound.FULL_REPORT,
                PreloadedCoverageReportLoadStrategy(fullCoverageData, binaryReports, intellijSourceInputs),
            )
        } else {
            deltaReportLoadStrategy
        }
    }

    private fun buildBinaryReports(reportContext: ReportContext): List<BinaryReport> {
        return reportContext.deltaCoverageConfig.binaryCoverageFiles.filter { it.exists() }.map { binaryCoverageFile ->
            BinaryReport(binaryCoverageFile, null)
        }
    }

    // TODO: build a new class
    private fun loadFullCoverageData(
        binaryReports: List<BinaryReport>,
        intellijSourceInputs: IntellijSourceInputs
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
        val directoryEntryProcessor = DirectoryEntryProcessor(excludePatterns)
        intellijSourceInputs.classesFiles.asSequence()
            .flatMap(directoryEntryProcessor::traverseClasses)
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

    private class PreloadedCoverageReportLoadStrategy(
        private val coverageData: ProjectData,
        binaryReports: List<BinaryReport>,
        intellijSourceInputs: IntellijSourceInputs
    ) : ReportLoadStrategy(
        binaryReports,
        intellijSourceInputs.classesFiles,
        intellijSourceInputs.sourcesFiles,
    ) {
        override fun loadProjectData(): ProjectData = coverageData
    }
}

data class JvmClass(
    val className: String,
    val file: File,
)

class DirectoryEntryProcessor(excludePatterns: Set<String>) {

    private val excludeRegexes: List<Regex> = excludePatterns.map { it.toRegex() }

    fun traverseClasses(rootClassesDir: File): Sequence<JvmClass> { // TODO: how jacoco resolve className from List<Files> ?
        if (!shouldInclude(rootClassesDir)) {
            return emptySequence()
        }

        return sequence {
            val traverseQueue: Deque<TraverseCandidate> = LinkedList()
            traverseQueue += collectEntriesFromDir("", rootClassesDir)

            while (traverseQueue.isNotEmpty()) {
                val candidate: TraverseCandidate = traverseQueue.pollFirst()!!
                if (candidate.file.isFile) {
                    if (shouldExclude(candidate.file)) {
                        continue
                    }
                    val className = candidate.resolveClassName()
                    yield(JvmClass(className, candidate.file))
                } else {
                    traverseQueue += collectEntriesFromDir(candidate.resolvePrefixFromThis(), candidate.file)
                }
            }
        }
    }

    private fun collectEntriesFromDir(prefix: String, dir: File): Sequence<TraverseCandidate> =
        (dir.listFiles()?.asSequence() ?: sequenceOf())
            .filter(::shouldInclude)
            .sortedWith { file1, file2 ->
                file1.nameWithoutExtension.compareTo(file2.nameWithoutExtension)
            }
            .map { file ->
                TraverseCandidate(prefix, file)
            }

    private fun shouldInclude(file: File): Boolean = when {
        file.isDirectory -> true
        file.extension == "class" -> true
        else -> false
    }

    private fun shouldExclude(file: File): Boolean = excludeRegexes.any {
        it.matches(file.absolutePath)
    }

    data class TraverseCandidate(val prefix: String, val file: File) {

        fun resolveClassName(): String = if (prefix.isEmpty()) {
            file.nameWithoutExtension
        } else {
            "$prefix.${file.nameWithoutExtension}"
        }

        fun resolvePrefixFromThis(): String = resolveClassName()
    }
}
