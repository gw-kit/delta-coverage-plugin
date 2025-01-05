package io.github.surpsg.deltacoverage.report.textual

internal object CoverageDataTransforming {

    private const val LAMBDA_CLASS_SUFFIX = "{...}"
    private const val LAMBDA_CLASS_PREFIX = ".new"

    fun transform(data: List<RawCoverageData>): List<RawCoverageData> {
        return foldLambdaClasses(data)
            .sortedWith(
                compareBy<RawCoverageData>(
                    { it.branches.ratio },
                    { it.lines.ratio },
                    { it.instr.ratio },
                ).reversed()
            )
    }

    private fun foldLambdaClasses(data: List<RawCoverageData>): List<RawCoverageData> {
        return data.asSequence()
            .map { it.minimizeLambdaClass() }
            .groupBy { it.aClass }
            .map {
                it.value.reduce { acc, rawCoverageData -> acc.merge(rawCoverageData) }
            }
    }

    private fun RawCoverageData.minimizeLambdaClass(): RawCoverageData {
        val thisData = this
        return if (thisData.aClass.endsWith(LAMBDA_CLASS_SUFFIX)) {
            val indexTo = thisData.aClass.indexOf(LAMBDA_CLASS_PREFIX)
            val minimizedClassName = thisData.aClass.substring(0, indexTo)
            thisData.updateName(minimizedClassName)
        } else {
            this
        }
    }

    private fun RawCoverageData.updateName(newName: String) = RawCoverageData {
        aClass = newName
        instr(instr.covered, instr.total)
        lines(lines.covered, lines.total)
        branches(branches.covered, branches.total)
    }
}
