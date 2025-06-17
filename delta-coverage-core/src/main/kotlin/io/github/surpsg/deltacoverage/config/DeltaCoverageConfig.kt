package io.github.surpsg.deltacoverage.config

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

@DslMarker
internal annotation class DeltaCoverageConfigMarker

class CoverageRulesConfig private constructor(
    private val violationRules: List<ViolationRule>,
    val failOnViolation: Boolean,
) {

    /**
     * Returns violation rules map associated with coverage entity.
     * A rule is ignored if min coverage ration is not greater than zero.
     * If multiple rules with the same coverage entity exists then the latest is chosen.
     *
     * @return coverage entity to its violation rule map.
     */
    val entitiesRules: Map<CoverageEntity, ViolationRule>
        get() = violationRules.reversed()
            .asSequence()
            .distinct()
            .filter { it.minCoverageRatio > 0.0 }
            .associateBy { it.coverageEntity }

    override fun toString(): String {
        return "CoverageRulesConfig(violationRules=${entitiesRules.values}, failOnViolation=$failOnViolation)"
    }

    @DeltaCoverageConfigMarker
    class Builder internal constructor() {
        val violationRules: MutableList<ViolationRule> = mutableListOf()
        var failOnViolation: Boolean = false

        fun build(): CoverageRulesConfig = CoverageRulesConfig(
            violationRules.toList(),
            failOnViolation
        )
    }

    companion object {

        operator fun invoke(builder: Builder.() -> Unit = {}): CoverageRulesConfig = Builder().apply(builder).build()
    }
}

class ViolationRule private constructor(
    val coverageEntity: CoverageEntity,
    val minCoverageRatio: Double,
    val entityCountThreshold: Int?
) {

    init {
        require(entityCountThreshold == null || entityCountThreshold > 0) {
            "$coverageEntity count threshold must be null or greater than zero but was $entityCountThreshold."
        }
        require(minCoverageRatio in 0.0..1.0) {
            "minCoverageRatio must be in range 1..0 but was $minCoverageRatio."
        }
    }

    override fun toString(): String {
        return "ViolationRule(coverageEntity=$coverageEntity" +
                ", minCoverageRatio=$minCoverageRatio, entityCountThreshold=$entityCountThreshold)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ViolationRule

        return coverageEntity == other.coverageEntity
    }

    override fun hashCode(): Int {
        return coverageEntity.hashCode()
    }

    @DeltaCoverageConfigMarker
    class Builder internal constructor() {
        var coverageEntity: CoverageEntity? = null
        var minCoverageRatio: Double = 0.0
        var entityCountThreshold: Int? = null

        fun build(): ViolationRule = ViolationRule(
            coverageEntity ?: error("Coverage entity was not set"),
            minCoverageRatio,
            entityCountThreshold
        )
    }

    companion object {

        operator fun invoke(customize: Builder.() -> Unit): ViolationRule =
            Builder().apply(customize).build()
    }
}

enum class CoverageEntity {
    INSTRUCTION, BRANCH, LINE
}

@Suppress("LongParameterList")
class ReportsConfig private constructor(
    val html: ReportConfig,
    val xml: ReportConfig,
    val console: ReportConfig,
    val markdown: ReportConfig,
    val baseReportDir: String,
    val fullCoverageReport: Boolean,
) {
    internal lateinit var view: String
    lateinit var summaries: Map<ReportBound, Path>

    override fun toString(): String = "ReportsConfig(html=$html, xml=$xml, console=$console," +
            " baseReportDir='$baseReportDir', fullCoverageReport=$fullCoverageReport)"

    @DeltaCoverageConfigMarker
    class Builder internal constructor() {
        var html: ReportConfig = ReportConfig {}
        var xml: ReportConfig = ReportConfig {}

        var console: ReportConfig = ReportConfig {}
        var markdown: ReportConfig = ReportConfig {}
        var baseReportDir: String = ""
        var fullCoverageReport: Boolean = false

        fun build(): ReportsConfig = ReportsConfig(
            html, xml, console, markdown, baseReportDir, fullCoverageReport,
        )
    }

    companion object {

        operator fun invoke(builder: Builder.() -> Unit): ReportsConfig =
            Builder().apply(builder).build()
    }
}

class ReportConfig private constructor(
    val enabled: Boolean,
    val outputFileName: String
) {

    override fun toString(): String {
        return "ReportConfig(enabled=$enabled, outputFileName='$outputFileName')"
    }

    @DeltaCoverageConfigMarker
    class Builder internal constructor() {
        var enabled: Boolean = false
        var outputFileName: String = ""

        fun build(): ReportConfig = ReportConfig(enabled, outputFileName)
    }

    companion object {

        operator fun invoke(builder: Builder.() -> Unit): ReportConfig {
            return Builder().apply(builder).build()
        }
    }
}

@Suppress("LongParameterList")
class DeltaCoverageConfig private constructor(
    val coverageEngine: CoverageEngine,
    val view: String,
    val diffSource: DiffSource,
    val reportsConfig: ReportsConfig,
    val coverageRulesConfig: CoverageRulesConfig,
    val binaryCoverageFiles: Set<File>,
    val classFiles: Set<File>,
    val excludeClasses: Set<String>,
    val classRoots: Set<File>,
    val sourceFiles: Set<File>,
) {


    override fun toString(): String {
        return "DeltaCoverageConfig(" +
                "view='$view'" +
                ", diffSource=${diffSource.sourceDescription}" +
                ", reportsConfig=$reportsConfig" +
                ", coverageRulesConfig=$coverageRulesConfig" +
                ", binaryCoverageFiles=${binaryCoverageFiles.stringifyLongCollection()}" +
                ", classFiles=${classFiles.stringifyLongCollection()}" +
                ", sourceFiles=${sourceFiles.stringifyLongCollection()}" +
                ")"
    }

    @DeltaCoverageConfigMarker
    class Builder internal constructor() {
        var fileSystem: FileSystem = FileSystems.getDefault()
        var coverageEngine: CoverageEngine = CoverageEngine.JACOCO
        var viewName: String = "delta-coverage-report"
        var diffSource: DiffSource? = null
        var reportsConfig: ReportsConfig = ReportsConfig {}
        var coverageRulesConfig: CoverageRulesConfig = CoverageRulesConfig()
        val binaryCoverageFiles: MutableSet<File> = mutableSetOf()
        val classFiles: MutableSet<File> = mutableSetOf()
        val excludeClasses: MutableSet<String> = mutableSetOf()
        val classRoots: MutableSet<File> = mutableSetOf()
        val sourceFiles: MutableSet<File> = mutableSetOf()

        fun build(): DeltaCoverageConfig = DeltaCoverageConfig(
            coverageEngine,
            viewName.apply {
                require(isNotBlank()) { "View name must be not blank" }
            },
            requireNotNull(diffSource) {
                "'${::diffSource.name}' is not configured"
            },
            finalizedReportsConfig(),
            coverageRulesConfig,
            binaryCoverageFiles.toSet(),
            classFiles.toSet(),
            excludeClasses.toSet(),
            classRoots.toSet(),
            sourceFiles.toSet()
        )

        private fun finalizedReportsConfig(): ReportsConfig = reportsConfig.apply {
            view = viewName

            val deltaSummaryFileName = "${viewName}-${SUMMARY_REPORT_FILE_NAME}"
            val fullSummaryFileName = "full-coverage-${deltaSummaryFileName}"
            val baseDir = fileSystem.getPath(baseReportDir)
            summaries = mapOf<ReportBound, Path>(
                ReportBound.DELTA_REPORT to baseDir.resolve(deltaSummaryFileName),
                ReportBound.FULL_REPORT to baseDir.resolve(fullSummaryFileName),
            )
        }
    }

    private fun <T> Iterable<T>.stringifyLongCollection(): String {
        return joinToString(prefix = "[", separator = ", ", postfix = "]", limit = MAX_ITEMS_TO_STRINGIFY)
    }

    companion object {
        private const val MAX_ITEMS_TO_STRINGIFY = 3

        const val SUMMARY_REPORT_FILE_NAME = "summary.json"

        operator fun invoke(customize: Builder.() -> Unit): DeltaCoverageConfig {
            return Builder().apply(customize).build()
        }
    }
}
