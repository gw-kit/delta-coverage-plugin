package io.github.surpsg.deltacoverage.report.textual

internal object ReportsConstants {
    const val MAX_CLASS_COLUMN_LENGTH = 100
    const val SHRINK_PLACEHOLDER = "..."

    const val PERCENT_MULTIPLIER = 100

    const val NA_VALUE = ""
    const val SUCCESS_COV_CHAR = "✔"
    const val FAILURE_COV_CHAR = "✖"
    const val SUCCESS_COV_ICON = "\uD83D\uDFE2" // 🟢
    const val FAILURE_COV_ICON = "\uD83D\uDD34" // 🔴
    const val SUCCESS_COV_PATTERN = "$SUCCESS_COV_CHAR %s"
    const val FAILURE_COV_PATTERN = "$FAILURE_COV_CHAR %s"

    const val CLASS_H = "Class"
    const val LINES_H = "Lines"
    const val BRANCHES_H = "Branches"
    const val INSTR_H = "Instr."
    val INDEXED_HEADERS: Map<String, Int> = sequenceOf(CLASS_H, LINES_H, BRANCHES_H, INSTR_H)
        .mapIndexed { index, header -> header to index }
        .associate { it }
    val HEADERS: List<String> = INDEXED_HEADERS.keys
        .sortedBy { header -> INDEXED_HEADERS.getValue(header) }

}
