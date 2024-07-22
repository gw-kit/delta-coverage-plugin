package io.github.surpsg.deltacoverage.report

@Suppress("MagicNumber")
enum class ReportType(val priority: Int) {
    CONSOLE(1),
    HTML(2),
    MARKDOWN(3),
    XML(4),

    @Deprecated("CSV will be removed soon.")
    CSV(Int.MAX_VALUE),
}
