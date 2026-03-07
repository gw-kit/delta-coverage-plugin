package io.github.gwkit.testimpact.gradle.sampling.testmapping.report

import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.FlamegraphData
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.FlamegraphDataCollector
import one.convert.Arguments
import one.convert.FlameGraph
import java.io.File
import java.io.PrintStream
import java.io.StringReader

/**
 * Generates a self-contained flamegraph HTML report using async-profiler's canvas renderer.
 *
 * Accepts pre-collected [FlamegraphData] (collapsed stacks) and feeds them into
 * async-profiler's [FlameGraph] for rendering.
 */
internal object AsyncProfilerFlamegraphReporter : Reporter {

    private const val FILE_NAME = "flamegraph.html"
    private const val TITLE = "Execution Profile"

    override fun write(context: ReportContext): File {
        val flamegraphData = FlamegraphDataCollector.collect(context.jfrFiles, testClasses = context.testClasses)
        val collapsed = flamegraphData.collapsedStacks.entries
            .joinToString("\n") { (stack, count) -> "$stack $count" }

        val fg = FlameGraph(Arguments("--title", TITLE))
        fg.parseCollapsed(StringReader(collapsed))

        return context.config.outputDir.resolve(FILE_NAME).apply {
            PrintStream(this, "UTF-8").use { fg.dump(it) }
        }
    }
}
