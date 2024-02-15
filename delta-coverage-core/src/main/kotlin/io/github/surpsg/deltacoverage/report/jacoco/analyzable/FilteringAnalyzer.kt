package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.diff.parse.ClassFile
import io.github.surpsg.deltacoverage.lib.jvm.readClassFileName
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.IClassCoverage
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.internal.analysis.ClassCoverageImpl
import org.jacoco.core.internal.analysis.FilteringClassAnalyzer
import org.jacoco.core.internal.analysis.StringPool
import org.jacoco.core.internal.analysis.filter.IFilter
import org.jacoco.core.internal.data.CRC64
import org.jacoco.core.internal.flow.ClassProbesAdapter
import org.jacoco.core.internal.instr.InstrSupport
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.IOException

class FilteringAnalyzer(
    private val executionData: ExecutionDataStore,
    private val coverageVisitor: ICoverageVisitor,
    private val classFilter: (ClassFile) -> Boolean,
    private val customFilterProvider: (IClassCoverage) -> IFilter
) : Analyzer(executionData, coverageVisitor) {

    override fun analyzeClass(buffer: ByteArray, location: String) {
        try {
            analyzeClass(buffer)
        } catch (cause: RuntimeException) {
            throw analyzerError(location, cause)
        }
    }

    private fun analyzeClass(source: ByteArray) {
        val classId = CRC64.classId(source)
        val reader = InstrSupport.classReaderFor(source)
        if (reader.access and Opcodes.ACC_MODULE != 0) {
            return
        }
        if (reader.access and Opcodes.ACC_SYNTHETIC != 0) {
            return
        }
        val shouldComputeClassCoverage = readClassFileName(source)
            ?.let { ClassFile(it, reader.className) }
            ?.let { classFilter(it) }
            ?: false
        if (shouldComputeClassCoverage) {
            reader.accept(
                createAnalyzingVisitor(classId, reader.className),
                0
            )
        }
    }

    private fun createAnalyzingVisitor(classid: Long, className: String): ClassVisitor {
        val data = executionData.get(classid)

        val (probes, noMatch) = if (data == null) {
            ExecutionInfo(null, executionData.contains(className))
        } else {
            ExecutionInfo(data.probes, false)
        }

        return ClassProbesAdapter(
            buildClassAnalyzer(
                ClassCoverageImpl(className, classid, noMatch),
                probes
            ),
            false
        )
    }

    private fun buildClassAnalyzer(
        coverage: ClassCoverageImpl,
        probes: BooleanArray?
    ): FilteringClassAnalyzer {
        return object : FilteringClassAnalyzer(
            coverage,
            probes,
            StringPool(),
            customFilterProvider(coverage)
        ) {
            override fun visitEnd() {
                super.visitEnd()
                coverageVisitor.visitCoverage(coverage)
            }
        }
    }

    private fun analyzerError(location: String, cause: Exception): IOException {
        return IOException("Error while analyzing $location.").apply {
            initCause(cause)
        }
    }

    private data class ExecutionInfo(
        val probes: BooleanArray?,
        val noMatch: Boolean
    )
}
