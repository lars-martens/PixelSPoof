package com.kashi.caimanspoof

import android.util.Log
import java.io.File
import java.lang.StringBuilder

class KernelSpoofer {

    private val TAG = "KernelSpoofer"

    // This function will be called from MainHook.kt
    fun applyKernelSpoofing(profile: DeviceProfile): Boolean {
        Log.i(TAG, "Attempting to apply kernel-level spoofing...")
        if (!isKernelSUActive()) {
            Log.e(TAG, "KernelSU not active. Cannot apply kernel-level spoofing.")
            return false
        }

        val commands = generateResetpropCommands(profile)
        if (commands.isEmpty()) {
            Log.e(TAG, "Failed to generate resetprop commands.")
            return false
        }

        return executeKernelCommands(commands)
    }

    private fun isKernelSUActive(): Boolean {
        // Your existing logic from KernelSUIntegration.kt
        // Check for su access via KernelSU
        return try {
            val process = Runtime.getRuntime().exec("su -c 'echo test'")
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for KernelSU: ${e.message}")
            false
        }
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

    private fun executeKernelCommands(commands: List<String>): Boolean {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            val sb = StringBuilder()
            commands.forEach { sb.append("$it\n") }
            os.write(sb.toString().toByteArray())
            os.flush()
            os.close()
            return process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute kernel commands: ${e.message}")
            return false
        }
    }
}
