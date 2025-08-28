package com.kashi.caimanspoof

import android.util.Log
import java.io.File
import java.lang.StringBuilder

class KernelSpoofer {

    private val TAG = "KernelSpoofer"

    // This function will be called from MainHook.kt
    fun applyKernelSpoofing(profile: DeviceProfile): Boolean {
        Log.i(TAG, "Attempting to apply kernel-level spoofing...")
        val suPath = findSuPath()
        if (suPath == null) {
            Log.e(TAG, "No su binary found in known locations. Cannot apply kernel-level spoofing.")
            return false
        }

        val commands = generateResetpropCommands(profile)
        if (commands.isEmpty()) {
            Log.e(TAG, "Failed to generate resetprop commands.")
            return false
        }

        return executeKernelCommands(commands, suPath)
    }

    private fun isKernelSUActive(): Boolean {
        // Deprecated: use findSuPath() and explicit exec with returned path.
        return findSuPath() != null
    }

    /**
     * Look for a usable su binary in common locations and return its path or null.
     */
    private fun findSuPath(): String? {
        val candidates = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/magisk/su",
            "/sbin/k-su",
            "/system/bin/ksu",
            "/system/xbin/ksu"
        )

        for (p in candidates) {
            try {
                val f = java.io.File(p)
                if (f.exists() && f.canExecute()) return p
            } catch (_: Exception) {
            }
        }

        // Fallback: try platform 'which su'
        try {
            val which = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", "which su"))
            val out = which.inputStream.bufferedReader().readText().trim()
            which.waitFor()
            if (out.isNotBlank()) return out
        } catch (_: Exception) {
        }

        return null
    }

    // A central place to generate all spoofing commands
    private fun generateResetpropCommands(profile: DeviceProfile): List<String> {
        val commands = mutableListOf<String>()
        // Use the profile data to create the commands dynamically
        commands.add("resetprop ro.product.manufacturer \"${profile.manufacturer}\"")
        commands.add("resetprop ro.product.brand \"${profile.brand}\"")
        commands.add("resetprop ro.product.device \"${profile.device}\"")
        commands.add("resetprop ro.product.model \"${profile.model}\"")
        commands.add("resetprop ro.build.fingerprint \"${profile.fingerprint}\"")
        commands.add("resetprop ro.build.id \"${profile.buildId}\"")
        commands.add("resetprop ro.build.version.release \"16\"")
        commands.add("resetprop ro.build.version.sdk \"36\"")
        commands.add("resetprop ro.build.version.security_patch \"${profile.securityPatch}\"")
        // Add all other properties from your device_profiles.json
        // ... (this list needs to be comprehensive)
        return commands
    }

    private fun executeKernelCommands(commands: List<String>, suPath: String): Boolean {
        try {
            // Start the chosen su binary and feed commands to its stdin
            val pb = ProcessBuilder(suPath)
            pb.redirectErrorStream(true)
            val process = pb.start()

            val os = process.outputStream
            val sb = StringBuilder()
            commands.forEach { sb.append("$it\n") }
            os.write(sb.toString().toByteArray())
            os.flush()
            os.close()

            val exit = process.waitFor()
            if (exit != 0) {
                Log.e(TAG, "su process exited with code $exit")
                // Attempt to read any output for debugging
                try {
                    val out = process.inputStream.bufferedReader().readText()
                    Log.e(TAG, "su output: $out")
                } catch (_: Exception) {
                }
            }
            return exit == 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute kernel commands: ${e.message}")
            return false
        }
    }
}
