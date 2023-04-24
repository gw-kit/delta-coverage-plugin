package io.github.surpsg.deltacoverage.gradle

import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.nio.file.Paths
import javax.inject.Inject

open class DeltaCoverageConfiguration @Inject constructor(
    objectFactory: ObjectFactory,
) {

    @Optional
    @InputFiles
    var jacocoExecFiles: FileCollection? = null

    @Optional
    @InputFiles
    var classesDirs: FileCollection? = null

    @Optional
    @InputFiles
    var srcDirs: FileCollection? = null

    @Input
    val excludeClasses: ListProperty<String> = objectFactory
        .listProperty(String::class.javaObjectType)
        .convention(emptyList())

    @Nested
    val diffSource: DiffSourceConfiguration = DiffSourceConfiguration(objectFactory)

    @Nested
    val reportConfiguration: ReportsConfiguration = ReportsConfiguration(objectFactory)

    @Nested
    val violationRules: ViolationRules = ViolationRules(objectFactory)

    fun reports(action: Action<in ReportsConfiguration>) {
        action.execute(reportConfiguration)
    }

    fun violationRules(action: Action<in ViolationRules>) {
        action.execute(violationRules)
    }

    fun diffSource(action: Action<in DiffSourceConfiguration>) {
        action.execute(diffSource)
    }

    override fun toString(): String {
        return "DeltaCoverageConfiguration(" +
                "jacocoExecFiles=$jacocoExecFiles, " +
                "classesDirs=$classesDirs, " +
                "srcDirs=$srcDirs, " +
                "excludeClasses=${excludeClasses.get()}, " +
                "diffSource=$diffSource, " +
                "reportConfiguration=$reportConfiguration, " +
                "violationRules=$violationRules)"
    }
}

open class DiffSourceConfiguration(
    objectFactory: ObjectFactory,
) {

    @Input
    val file: Property<String> = objectFactory.stringProperty("")

    @Input
    val url: Property<String> = objectFactory.stringProperty("")

    @Nested
    val git: GitConfiguration = GitConfiguration(objectFactory)

    override fun toString(): String {
        return "DiffSourceConfiguration(file='${file.get()}', url='${url.get()}', git=$git)"
    }
}

open class GitConfiguration(
    objectFactory: ObjectFactory,
) {

    @Input
    val diffBase: Property<String> = objectFactory.stringProperty("")

    infix fun compareWith(diffBase: String) {
        this.diffBase.set(diffBase)
    }

    override fun toString(): String {
        return "GitConfiguration(diffBase='${diffBase.get()}')"
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
    val csv: Property<Boolean> = objectFactory.booleanProperty(false)

    @Input
    val baseReportDir: Property<String> = objectFactory.stringProperty {
        Paths.get("build", "reports", "jacoco").toString()
    }

    @Input
    val fullCoverageReport: Property<Boolean> = objectFactory.booleanProperty(false)

    override fun toString() = "ReportsConfiguration(" +
            "html=${html.get()}, " +
            "xml=${xml.get()}, " +
            "csv=${csv.get()}, " +
            "baseReportDir='$baseReportDir'"
}

open class ViolationRules(
    objectFactory: ObjectFactory,
) {

    @Input
    val minLines: Property<Double> = objectFactory.doubleProperty(0.0)

    @Input
    val minBranches: Property<Double> = objectFactory.doubleProperty(0.0)

    @Input
    val minInstructions: Property<Double> = objectFactory.doubleProperty(0.0)

    @Input
    val failOnViolation: Property<Boolean> = objectFactory.booleanProperty(false)

    infix fun failIfCoverageLessThan(minCoverage: Double) {
        minLines.set(minCoverage)
        minBranches.set(minCoverage)
        minInstructions.set(minCoverage)
        failOnViolation.set(true)
    }

    override fun toString(): String {
        return "ViolationRules(" +
                "minLines=${minLines.get()}, " +
                "minBranches=${minBranches.get()}, " +
                "minInstructions=${minInstructions.get()}, " +
                "failOnViolation=${failOnViolation.get()}" +
                ")"
    }
}

private fun ObjectFactory.booleanProperty(default: Boolean): Property<Boolean> {
    return property(Boolean::class.javaObjectType).convention(default)
}

private fun ObjectFactory.doubleProperty(default: Double): Property<Double> {
    return property(Double::class.javaObjectType).convention(default)
}

private fun ObjectFactory.stringProperty(default: String): Property<String> {
    return property(String::class.javaObjectType).convention(default)
}

private fun ObjectFactory.stringProperty(default: () -> String): Property<String> {
    return property(String::class.javaObjectType).convention(
        default()
    )
}
