package io.github.surpsg.deltacoverage.sampling

/**
 * Represents a single stack sample captured during test execution.
 *
 * @property timestamp Unix timestamp in milliseconds when the sample was captured
 * @property testId Identifier of the test that was running when sample was captured
 * @property threadName Name of the sampled thread
 * @property frames Stack frames from the sample, filtered to application code
 */
data class Sample(
    val timestamp: Long,
    val testId: TestIdentifier,
    val threadName: String,
    val frames: List<StackFrame>,
)

/**
 * Identifies a specific test method.
 *
 * @property className Fully qualified class name of the test
 * @property methodName Name of the test method
 * @property displayName Human-readable display name of the test
 */
data class TestIdentifier(
    val className: String,
    val methodName: String,
    val displayName: String,
) {
    /**
     * Returns a compact string representation suitable for JSON output.
     */
    fun toCompactString(): String = "$className#$methodName"
}

/**
 * Represents a single stack frame.
 *
 * @property className Fully qualified class name
 * @property methodName Method name
 * @property lineNumber Line number in source file, or -1 if unavailable
 */
data class StackFrame(
    val className: String,
    val methodName: String,
    val lineNumber: Int,
) {
    companion object {
        /**
         * Creates a StackFrame from a Java StackTraceElement.
         */
        fun fromStackTraceElement(element: StackTraceElement): StackFrame = StackFrame(
            className = element.className,
            methodName = element.methodName,
            lineNumber = element.lineNumber,
        )
    }
}
