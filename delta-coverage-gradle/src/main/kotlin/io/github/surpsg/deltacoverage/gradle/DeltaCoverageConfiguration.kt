package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTaskConfigurer
import io.github.surpsg.deltacoverage.gradle.utils.booleanProperty
import io.github.surpsg.deltacoverage.gradle.utils.doubleProperty
import io.github.surpsg.deltacoverage.gradle.utils.map
import io.github.surpsg.deltacoverage.gradle.utils.new
import io.github.surpsg.deltacoverage.gradle.utils.stringProperty
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.nio.file.Paths
import javax.inject.Inject

open class DeltaCoverageConfiguration @Inject constructor(
    objectFactory: ObjectFactory,
) {

    @Nested
    val coverage: Coverage = objectFactory.new<Coverage>()

    @Optional
    @InputFiles
    var classesDirs: FileCollection? = null

    @Optional
    @InputFiles
    var sources: FileCollection? = null

    @Input
    val excludeClasses: ListProperty<String> = objectFactory
        .listProperty(String::class.javaObjectType)
        .convention(emptyList())

    @Nested
    val diffSource: DiffSourceConfiguration = DiffSourceConfiguration(objectFactory)

    @Nested
    val reportConfiguration: ReportsConfiguration = ReportsConfiguration(objectFactory)

    @Internal
    val reportViews: NamedDomainObjectContainer<ReportView> =
        objectFactory.domainObjectContainer(ReportView::class.java) { name ->
            objectFactory.newInstance(ReportView::class.java, name, objectFactory)
        }.apply { maybeCreate(DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME) }

    fun coverage(action: Action<in Coverage>): Unit = action.execute(coverage)

    fun reports(action: Action<in ReportsConfiguration>) {
        action.execute(reportConfiguration)
    }

    fun diffSource(action: Action<in DiffSourceConfiguration>) {
        action.execute(diffSource)
    }

    /**
     * Configures a [ReportView] for the report.
     * If the view is not found, it will be created.
     *
     * @param name The name of the view.
     * @param action The configuration action.
     */
    fun view(name: String, action: Action<in ReportView>) {
        reportViews.maybeCreate(name)
        reportViews.named(name, action)
    }

    override fun toString(): String {
        return "DeltaCoverageConfiguration(" +
                "coverageEngine=${coverage.engine.get()}, " +
                "classesDirs=$classesDirs, " +
                "excludeClasses=${excludeClasses.get()}, " +
                "diffSource=$diffSource, " +
                "reportConfiguration=$reportConfiguration)"
    }
}

open class ReportView @Inject constructor(
    private val name: String,
    objectFactory: ObjectFactory,
) : Named {

    @Input
    val enabled: Property<Boolean> = objectFactory.property(Boolean::class.javaObjectType)

    @Optional
    @InputFiles
    var coverageBinaryFiles: FileCollection? = null

    @Nested
    val violationRules: ViolationRules = objectFactory.new<ViolationRules>()

    @Input
    val matchClasses: ListProperty<String> = objectFactory
        .listProperty(String::class.javaObjectType)
        .convention(emptyList())

    fun violationRules(action: Action<in ViolationRules>) {
        action.execute(violationRules)
    }

    override fun toString(): String {
        return "View(" +
                "coverageBinaryFiles=$coverageBinaryFiles, " +
                "violationRules=$violationRules)"
    }

    override fun getName(): String = name
}

open class Coverage @Inject constructor(
    objectFactory: ObjectFactory,
) {
    @Input
    val engine: Property<CoverageEngine> = objectFactory.property(CoverageEngine::class.java)
        .convention(CoverageEngine.JACOCO)

    @Input
    val autoApplyPlugin: Property<Boolean> = objectFactory.booleanProperty(true)
}

open class DiffSourceConfiguration(
    objectFactory: ObjectFactory,
) {

    /**
     * The file path to the diff file.
     */
    @Input
    val file: Property<String> = objectFactory.stringProperty("")

    /**
     * The URL to the diff file.
     */
    @Input
    val url: Property<String> = objectFactory.stringProperty("")

    /**
     * Git configuration for the diff source.
     */
    @Nested
    val git: GitConfiguration = GitConfiguration(objectFactory)

    /**
     * Configures Git as a source of the diff.
     */
    fun byGit(action: Action<in GitConfiguration>) {
        action.execute(git)
    }

    override fun toString(): String {
        return "DiffSourceConfiguration(file='${file.get()}', url='${url.get()}', git=$git)"
    }
}

open class GitConfiguration(
    objectFactory: ObjectFactory,
) {

    /**
     * The base branch to compare with.
     */
    @Input
    val diffBase: Property<String> = objectFactory.stringProperty("")

    /**
     * Use native git to generate the diff file.
     */
    @Input
    val useNativeGit: Property<Boolean> = objectFactory.booleanProperty(false)

    @get:Internal
    internal val nativeGitDiffFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Sets the base branch to compare with.
     */
    infix fun compareWith(diffBase: String) {
        this.diffBase.set(diffBase)
    }

    override fun toString(): String {
        return "GitConfiguration(diffBase='${diffBase.get()}', useNativeGit=${useNativeGit.get()})"
    }
}

open class ReportsConfiguration(
    objectFactory: ObjectFactory,
) {

    @Input
    val html: Property<Boolean> = objectFactory.booleanProperty(false)

    @Input
    val xml: Property<Boolean> = objectFactory.booleanProperty(false)

    @Input
    val console: Property<Boolean> = objectFactory.booleanProperty(true)

    @Input
    val markdown: Property<Boolean> = objectFactory.booleanProperty(false)

    @Input
    val baseReportDir: Property<String> = objectFactory.stringProperty {
        Paths.get("build", "reports").toString()
    }

    @Input
    val fullCoverageReport: Property<Boolean> = objectFactory.booleanProperty(false)

    override fun toString() = "ReportsConfiguration(" +
            "html=${html.get()}, " +
            "xml=${xml.get()}, " +
            "console=${console.get()}, " +
            "markdown=${markdown.get()}, " +
            "baseReportDir='${baseReportDir.get()}')"
}

enum class CoverageEntity {
    INSTRUCTION,
    BRANCH,
    LINE,
}

open class ViolationRules @Inject constructor(
    private val objectFactory: ObjectFactory,
) {

    @Nested
    val rules: MapProperty<CoverageEntity, ViolationRule> = objectFactory.map<CoverageEntity, ViolationRule>()

    init {
        rules.putAll(
            CoverageEntity.entries.associateWith {
                objectFactory.new<ViolationRule>()
            }
        )
    }

    @Input
    val failOnViolation: Property<Boolean> = objectFactory.booleanProperty(false)

    /**
     * Sets the minimum coverage ratio for all coverage entities.
     * Enables build failure on poor coverage.
     */
    infix fun failIfCoverageLessThan(minCoverage: Double) {
        failOnViolation.set(true)
        all { rule ->
            rule.minCoverageRatio.set(minCoverage)
        }
    }

    /**
     * Applies the given action to all [ViolationRule]s.
     *
     * Usage example:
     * ```kotlin
     * violationRules {
     *    all {
     *        minCoverageRatio.set(0.7d)
     *    }
     * }
     */
    fun all(action: Action<in ViolationRule>) {
        CoverageEntity.entries.forEach { coverageEntity -> coverageEntity(action) }
    }

    /**
     * Applies the given action to the [ViolationRule] for the given [CoverageEntity].
     * This operator is designed  to be used in the following way:
     * ```kotlin
     * violationRules {
     *     CoverageEntity.LINE {
     *         minCoverageRatio.set(0.7d)
     *     }
     * }
     * ```
     */
    operator fun CoverageEntity.invoke(action: Action<in ViolationRule>) = rule(this, action)

    /**
     * Applies the given action to the [ViolationRule] for the given [CoverageEntity].
     * This function is designed to be used in the following way:
     * ```kotlin
     * violationRules {
     *     rule(CoverageEntity.LINE) {
     *         minCoverageRatio.set(0.7d)
     *     }
     * }
     * ```
     */
    fun rule(coverageEntity: CoverageEntity, action: Action<in ViolationRule>) {
        val newViolationRule: ViolationRule = rules.get().getValue(coverageEntity)
        action.execute(newViolationRule)
    }

    override fun toString(): String =
        "ViolationRules(allRules=${rules.get()}, failOnViolation=${failOnViolation.get()})"
}

open class ViolationRule @Inject constructor(
    objectFactory: ObjectFactory,
) {

    @Input
    val minCoverageRatio: Property<Double> = objectFactory.doubleProperty(0.0)

    @Input
    @Optional
    val entityCountThreshold: Property<Int?> = objectFactory.property(Int::class.java)

    override fun toString(): String {
        return "ViolationRule(minCoverageRatio=${minCoverageRatio.get()}, " +
                "entityCountThreshold=${entityCountThreshold.orNull})"
    }
}
