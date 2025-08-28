package com.kashi.caimanspoof

/**
 * Runtime metrics and counters for diagnostics and validation
 */
class Metrics private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: Metrics? = null

        fun getInstance(): Metrics {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Metrics().also { INSTANCE = it }
            }
        }
    }

    @Volatile
    var propertyRequests: Int = 0
        private set

    @Volatile
    var propertiesSpoofed: Int = 0
        private set

    @Volatile
    var requestsIntercepted: Int = 0
        private set

    @Volatile
    var headersSpoofed: Int = 0
        private set

    @Volatile
    var sslBypassAvailable: Boolean = false
        private set

    @Volatile
    var systemPropertiesHookInstalled: Boolean = false
        private set

    fun incrementPropertyRequest() {
        propertyRequests++
    }

    fun incrementPropertySpoofed() {
        propertiesSpoofed++
    }

    fun incrementRequestsIntercepted() {
        requestsIntercepted++
    }

    fun incrementHeadersSpoofed() {
        headersSpoofed++
    }

    fun setSslBypassAvailable(v: Boolean) {
        sslBypassAvailable = v
    }

    fun setSystemPropertiesHookInstalled(v: Boolean) {
        systemPropertiesHookInstalled = v
    }

    fun recordPackageHook(pkg: String) {
        // Could store per-package counts; simplified for now
    }

    fun calculateNetworkSuccessRate(): Int {
        // Simple heuristic for display purposes
        val total = requestsIntercepted.takeIf { it > 0 } ?: 1
        val rate = (headersSpoofed * 100) / total
        return rate.coerceIn(0, 100)
    }
}
