package com.kashi.caimanspoof

import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Best-effort LSPosed/Xposed enablement diagnostics.
 * Note: We cannot fully query LSPosed manager from userspace reliably, but this offers helpful hints.
 */
object LSPosedDiagnostic {
    fun isModuleEnabledForPackage(packageName: String): Boolean {
        // Heuristic: if SystemProperties hook is installed and we've seen property requests, assume enabled
        val metrics = Metrics.getInstance()
        if (metrics.systemPropertiesHookInstalled && metrics.propertyRequests > 0) return true

        // Additional heuristic: check whether XposedBridge is present
        return try {
            val bridge = XposedHelpers.findClass("de.robv.android.xposed.XposedBridge", null)
            bridge != null
        } catch (e: Throwable) {
            false
        }
    }
}
