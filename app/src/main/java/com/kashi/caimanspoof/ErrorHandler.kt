package com.kashi.caimanspoof

/**
 * Centralized error handling and logging utility
 * Provides consistent error management across all spoofing components
 */
object ErrorHandler {

    /**
     * Execute operation with comprehensive error handling
     */
    fun safeExecute(
        operation: String,
        component: String = "Unknown",
        block: () -> Unit
    ) {
        try {
            block()
        } catch (e: Exception) {
            logError(operation, component, e)
            // Continue execution - don't let one failure break everything
        }
    }

    /**
     * Execute operation with result and error handling
     */
    fun <T> safeExecuteWithResult(
        operation: String,
        component: String = "Unknown",
        defaultValue: T,
        block: () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            logError(operation, component, e)
            defaultValue
        }
    }

    /**
     * Log error with consistent formatting
     */
    fun logError(operation: String, component: String, error: Exception) {
        StealthManager.stealthLog("❌ [$component] $operation failed: ${error.message}")
        error.printStackTrace()
    }

    /**
     * Validate operation prerequisites
     */
    fun validatePrerequisites(
        operation: String,
        vararg checks: Pair<String, () -> Boolean>
    ): Boolean {
        val failures = mutableListOf<String>()

        checks.forEach { (description, check) ->
            if (!check()) {
                failures.add(description)
            }
        }

        if (failures.isNotEmpty()) {
            StealthManager.stealthLog("⚠️ [$operation] Prerequisites not met: ${failures.joinToString(", ")}")
            return false
        }

        return true
    }

    /**
     * Log operation success
     */
    fun logSuccess(operation: String, component: String = "Unknown", details: String? = null) {
        val message = if (details != null) {
            "✅ [$component] $operation successful: $details"
        } else {
            "✅ [$component] $operation successful"
        }
        StealthManager.stealthLog(message)
    }

    /**
     * Log operation start
     */
    fun logStart(operation: String, component: String = "Unknown") {
        StealthManager.stealthLog("🚀 [$component] Starting: $operation")
    }

    /**
     * Log operation completion with timing
     */
    fun logCompletion(operation: String, component: String = "Unknown", startTime: Long = System.currentTimeMillis()) {
        val duration = System.currentTimeMillis() - startTime
        StealthManager.stealthLog("🏁 [$component] Completed: $operation (${duration}ms)")
    }
}
