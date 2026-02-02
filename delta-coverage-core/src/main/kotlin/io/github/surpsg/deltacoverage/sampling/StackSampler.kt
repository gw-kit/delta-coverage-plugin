package io.github.surpsg.deltacoverage.sampling

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Stack sampler that periodically captures stack traces during test execution.
 *
 * Thread-safe and designed to handle parallel test execution.
 */
class StackSampler(
    private val config: SamplingConfig = SamplingConfig(),
) {
    private val running = AtomicBoolean(false)
    private val threadToTest = ConcurrentHashMap<Long, TestIdentifier>()
    private val samples = ConcurrentLinkedQueue<Sample>()

    private var executor: ScheduledExecutorService? = null
    private var samplingTask: ScheduledFuture<*>? = null

    /**
     * Starts the sampling process.
     * Safe to call multiple times; subsequent calls are no-ops if already running.
     */
    fun start() {
        if (!running.compareAndSet(false, true)) {
            return
        }

        executor = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "delta-coverage-sampler").apply {
                isDaemon = true
            }
        }

        samplingTask = executor?.scheduleAtFixedRate(
            ::captureAllThreads,
            config.intervalMs,
            config.intervalMs,
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * Stops the sampling process and returns all collected samples.
     *
     * @return List of all samples collected during the sampling session
     */
    fun stop(): List<Sample> {
        if (!running.compareAndSet(true, false)) {
            return emptyList()
        }

        samplingTask?.cancel(false)
        executor?.shutdown()
        try {
            executor?.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        return samples.toList()
    }

    /**
     * Associates the current thread with a test.
     * Called when a test starts execution.
     *
     * @param testId The test identifier to associate with the current thread
     */
    fun setCurrentTest(testId: TestIdentifier) {
        threadToTest[Thread.currentThread().id] = testId
    }

    /**
     * Clears the test association for the current thread.
     * Called when a test finishes execution.
     */
    fun clearCurrentTest() {
        threadToTest.remove(Thread.currentThread().id)
    }

    /**
     * Returns true if the sampler is currently running.
     */
    fun isRunning(): Boolean = running.get()

    /**
     * Returns the current sample count.
     */
    fun sampleCount(): Int = samples.size

    private fun captureAllThreads() {
        val timestamp = System.currentTimeMillis()
        val allStackTraces = Thread.getAllStackTraces()

        for ((thread, stackTrace) in allStackTraces) {
            val testId = threadToTest[thread.id] ?: continue

            val filteredFrames = stackTrace
                .take(config.maxDepth)
                .filter { element -> shouldIncludeFrame(element) }
                .map { element -> StackFrame.fromStackTraceElement(element) }

            if (filteredFrames.isNotEmpty()) {
                samples.add(
                    Sample(
                        timestamp = timestamp,
                        testId = testId,
                        threadName = thread.name,
                        frames = filteredFrames,
                    )
                )
            }
        }
    }

    private fun shouldIncludeFrame(element: StackTraceElement): Boolean {
        val className = element.className
        return config.excludePackagePrefixes.none { prefix -> className.startsWith(prefix) }
    }

    companion object {
        private const val SHUTDOWN_TIMEOUT_MS = 1000L
    }
}
