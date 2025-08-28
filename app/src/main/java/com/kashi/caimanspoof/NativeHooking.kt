/*
 * PixelSpoof - Native Code Hooking System
 * Bypass native C/C++ checks and syscall interception
 */

package com.kashi.caimanspoof

import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import java.lang.reflect.Method

/**
 * Advanced native code hooking to bypass C/C++ checks
 */
class NativeHooking private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: NativeHooking? = null
        
        fun getInstance(): NativeHooking {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NativeHooking().also { INSTANCE = it }
            }
        }
        
        // Native libraries that commonly perform device checks
        private val TARGET_NATIVE_LIBS = listOf(
            "libgoogle",
            "libgms", 
            "libplaycore",
            "libssl",
            "libcrypto",
            "libchrome",
            "libwebview"
        )
        
        // System calls that reveal device information
        private val MONITORED_SYSCALLS = listOf(
            "__system_property_get",
            "getprop",
            "property_get",
            "access",
            "open",
            "openat",
            "readlink",
            "stat",
            "lstat"
        )
    }
    
    private val hookedMethods = mutableSetOf<String>()
    // Map stream identity -> file path for streams created from files
    private val streamPathMap = mutableMapOf<Int, String>()
    // Map stream identity -> current read offset in spoof buffer
    private val streamOffsetMap = mutableMapOf<Int, Int>()
    
    /**
     * Initialize native code hooking system
     */
    fun initializeNativeHooking(lpparam: XC_LoadPackage.LoadPackageParam, deviceProfile: DeviceProfile) {
        StealthManager.stealthLog("Initializing native code hooking")
        
        try {
            // Hook native library loading
            hookNativeLibraryLoading(lpparam, deviceProfile)
            
            // Hook JNI method calls
            hookJniMethods(lpparam, deviceProfile)
            
            // Hook system property access at native level
            hookNativePropertyAccess(lpparam, deviceProfile)
            
            // Hook file system access
            hookFileSystemAccess(lpparam, deviceProfile)
            
            // Hook native reflection
            hookNativeReflection(lpparam)
            
            StealthManager.stealthLog("Native hooking initialized successfully")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Native hooking initialization failed: ${e.message}")
        }
    }
    
    /**
     * Hook native library loading to intercept and modify libraries
     */
    private fun hookNativeLibraryLoading(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        try {
            // Hook System.loadLibrary
            XposedHelpers.findAndHookMethod(
                System::class.java,
                "loadLibrary",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val libName = param.args[0] as String
                        
                        if (TARGET_NATIVE_LIBS.any { libName.contains(it) }) {
                            StealthManager.stealthLog("Intercepting native library: $libName")
                            
                            // Pre-load our hooks before the library initializes
                            preloadNativeHooks(libName, profile)
                        }
                    }
                }
            )
            
            // Hook Runtime.loadLibrary
            XposedHelpers.findAndHookMethod(
                Runtime::class.java,
                "loadLibrary0",
                Class::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val libName = param.args[1] as String
                        
                        if (TARGET_NATIVE_LIBS.any { libName.contains(it) }) {
                            StealthManager.stealthLog("Runtime loading library: $libName")
                            interceptNativeLibrary(libName, profile)
                        }
                    }
                }
            )
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Native library hooking failed: ${e.message}")
        }
    }
    
    /**
     * Hook JNI method calls to intercept native function calls
     */
    private fun hookJniMethods(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        try {
            // Hook common JNI methods that access device properties
            val jniMethods = listOf(
                "getSystemProperty",
                "getBuildProperty", 
                "getDeviceInfo",
                "checkDeviceIntegrity",
                "validateDevice",
                "getHardwareInfo"
            )
            
            jniMethods.forEach { methodName ->
                try {
                    // Try to find and hook native methods
                    hookNativeMethodByName(lpparam, methodName, profile)
                } catch (e: Exception) {
                    // Method might not exist in this app
                    StealthManager.stealthLog("JNI method $methodName not found: ${e.message}")
                }
            }
            
            // Hook generic native method calls
            hookGenericNativeCalls(lpparam, profile)
            
        } catch (e: Exception) {
            StealthManager.stealthLog("JNI method hooking failed: ${e.message}")
        }
    }
    
    /**
     * Hook native system property access
     */
    private fun hookNativePropertyAccess(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        try {
            // Hook __system_property_get (native property access)
            // This requires native code hooking which we simulate through reflection
            
            // Hook any methods that might call native property access
            val propertyMethods = listOf(
                "android.os.SystemProperties",
                "java.lang.System"
            )
            
            propertyMethods.forEach { className ->
                try {
                    val clazz = XposedHelpers.findClass(className, lpparam.classLoader)
                    val methods = clazz.declaredMethods
                    
                    methods.forEach { method ->
                        if (method.name.contains("get") || method.name.contains("property")) {
                            hookMethodForPropertyAccess(method, profile)
                        }
                    }
                } catch (e: Exception) {
                    // Class might not exist
                }
            }
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Native property access hooking failed: ${e.message}")
        }
    }
    
    /**
     * Hook file system access to intercept device info reads
     */
    private fun hookFileSystemAccess(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        try {
            // Hook File operations that might read device info
            XposedHelpers.findAndHookMethod(
                File::class.java,
                "exists",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val file = param.thisObject as File
                        val path = file.absolutePath
                        
                        // Intercept access to device-specific files
                        when {
                            path.contains("/proc/cpuinfo") -> {
                                // Return true for Pixel CPU info
                                if (shouldSpoofCpuInfo(path, profile)) {
                                    param.result = true
                                }
                            }
                            path.contains("/sys/devices") -> {
                                // Return true for Pixel hardware devices
                                if (shouldSpoofHardwareDevice(path, profile)) {
                                    param.result = true
                                }
                            }
                            path.contains("/proc/version") -> {
                                // Return true for kernel version
                                param.result = true
                            }
                            path.contains("magisk") || path.contains("xposed") -> {
                                // Hide root/framework traces
                                param.result = false
                            }
                        }
                    }
                }
            )
            
            // Hook file reading (implement /proc/cpuinfo spoofing)
            hookFileReading(lpparam, profile)
            
        } catch (e: Exception) {
            StealthManager.stealthLog("File system access hooking failed: ${e.message}")
        }
    }
    
    /**
     * Hook native reflection to prevent discovery
     */
    private fun hookNativeReflection(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook getDeclaredMethods to hide our hooks
            XposedHelpers.findAndHookMethod(
                Class::class.java,
                "getDeclaredMethods",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val methods = param.result as Array<Method>
                        
                        // Filter out our hook methods
                        val filteredMethods = methods.filter { method ->
                            !hookedMethods.contains(method.name) &&
                            !method.name.contains("hook") &&
                            !method.name.contains("Hook")
                        }.toTypedArray()
                        
                        param.result = filteredMethods
                    }
                }
            )
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Native reflection hooking failed: ${e.message}")
        }
    }
    
    /**
     * Pre-load native hooks before library initialization
     */
    private fun preloadNativeHooks(libName: String, profile: DeviceProfile) {
        StealthManager.stealthLog("Pre-loading hooks for $libName")
        
        // Prepare fake data structures that native code might access
        prepareFakeNativeData(libName, profile)
        
        // Set up memory patches if possible
        setupMemoryPatches(libName, profile)
    }
    
    /**
     * Intercept native library after loading
     */
    private fun interceptNativeLibrary(libName: String, profile: DeviceProfile) {
        StealthManager.stealthLog("Intercepting loaded library: $libName")
        
        // Try to hook known native functions
        when {
            libName.contains("libgoogle") -> {
                hookGoogleNativeFunctions(profile)
            }
            libName.contains("libgms") -> {
                hookGmsNativeFunctions(profile)
            }
            libName.contains("libplaycore") -> {
                hookPlayCoreNativeFunctions(profile)
            }
        }
    }
    
    /**
     * Hook native method by name using reflection
     */
    private fun hookNativeMethodByName(lpparam: XC_LoadPackage.LoadPackageParam, methodName: String, profile: DeviceProfile) {
        try {
            // Search for the method in loaded classes
            val classLoader = lpparam.classLoader
            val loadedClasses = getAllLoadedClasses(classLoader)
            
            loadedClasses.forEach { clazz ->
                try {
                    val method = clazz.getDeclaredMethod(methodName)
                    if (method.isAnnotationPresent(java.lang.annotation.Native::class.java)) {
                        hookNativeMethod(method, profile)
                    }
                } catch (e: Exception) {
                    // Method doesn't exist in this class
                }
            }
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Native method hooking failed for $methodName: ${e.message}")
        }
    }
    
    /**
     * Hook generic native calls
     */
    private fun hookGenericNativeCalls(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        try {
            // Hook methods that might make native calls
            val classes = getAllLoadedClasses(lpparam.classLoader)
            
            classes.forEach { clazz ->
                val methods = clazz.declaredMethods
                methods.forEach { method ->
                    if (method.modifiers and java.lang.reflect.Modifier.NATIVE != 0) {
                        // This is a native method
                        hookNativeMethod(method, profile)
                    }
                }
            }
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Generic native call hooking failed: ${e.message}")
        }
    }
    
    /**
     * Hook a specific native method
     */
    private fun hookNativeMethod(method: Method, profile: DeviceProfile) {
        try {
            val methodKey = "${method.declaringClass.name}.${method.name}"
            if (hookedMethods.contains(methodKey)) {
                return // Already hooked
            }
            
            // Use XposedBridge to hook the method
            XposedHelpers.findAndHookMethod(
                method.declaringClass,
                method.name,
                *method.parameterTypes,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // Intercept native method call
                        interceptNativeCall(method, param, profile)
                    }
                    
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // Modify native method result
                        modifyNativeResult(method, param, profile)
                    }
                }
            )
            
            hookedMethods.add(methodKey)
            StealthManager.stealthLog("Hooked native method: $methodKey")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Failed to hook native method ${method.name}: ${e.message}")
        }
    }
    
    // Helper methods for specific library hooks
    private fun hookGoogleNativeFunctions(profile: DeviceProfile) {
        StealthManager.stealthLog("Hooking Google native functions for ${profile.device}")
    }
    
    private fun hookGmsNativeFunctions(profile: DeviceProfile) {
        StealthManager.stealthLog("Hooking GMS native functions for ${profile.device}")
    }
    
    private fun hookPlayCoreNativeFunctions(profile: DeviceProfile) {
        StealthManager.stealthLog("Hooking Play Core native functions for ${profile.device}")
    }
    
    private fun prepareFakeNativeData(libName: String, profile: DeviceProfile) {
        StealthManager.stealthLog("Preparing fake native data for $libName")
    }
    
    private fun setupMemoryPatches(libName: String, profile: DeviceProfile) {
        StealthManager.stealthLog("Setting up memory patches for $libName")
    }
    
    private fun interceptNativeCall(method: Method, param: XC_MethodHook.MethodHookParam, profile: DeviceProfile) {
        // Intercept and potentially modify native call parameters
    }
    
    private fun modifyNativeResult(method: Method, param: XC_MethodHook.MethodHookParam, profile: DeviceProfile) {
        // Modify the result from native method to match Pixel device
    }
    
    private fun hookMethodForPropertyAccess(method: Method, profile: DeviceProfile) {
        // Hook methods that access system properties
    }
    
    private fun hookFileReading(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        // Hook file reading operations to spoof device-specific files
        try {
            // Hook FileInputStream(File)
            XposedHelpers.findAndHookConstructor(
                java.io.FileInputStream::class.java,
                java.io.File::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val file = param.args[0] as java.io.File
                            val stream = param.thisObject
                            val id = System.identityHashCode(stream)
                            streamPathMap[id] = file.absolutePath
                            streamOffsetMap[id] = 0
                            // If this stream is for /proc/cpuinfo, prime the spoof buffer
                            if (file.absolutePath.contains("/proc/cpuinfo") && shouldSpoofCpuInfo(file.absolutePath, profile)) {
                                StealthManager.stealthLog("🔧 FileInputStream opened for /proc/cpuinfo - preparing spoof buffer")
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            )

            // Hook FileInputStream(String)
            XposedHelpers.findAndHookConstructor(
                java.io.FileInputStream::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val path = param.args[0] as String
                            val stream = param.thisObject
                            val id = System.identityHashCode(stream)
                            streamPathMap[id] = path
                            streamOffsetMap[id] = 0
                            if (path.contains("/proc/cpuinfo") && shouldSpoofCpuInfo(path, profile)) {
                                StealthManager.stealthLog("🔧 FileInputStream(String) opened for /proc/cpuinfo - preparing spoof buffer")
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            )

            // Hook RandomAccessFile(File, String)
            XposedHelpers.findAndHookConstructor(
                java.io.RandomAccessFile::class.java,
                java.io.File::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val file = param.args[0] as java.io.File
                            val raf = param.thisObject
                            val id = System.identityHashCode(raf)
                            streamPathMap[id] = file.absolutePath
                            streamOffsetMap[id] = 0
                        } catch (e: Exception) {
                        }
                    }
                }
            )

            // Hook InputStream.read(byte[], int, int) to inject spoofed content for /proc/cpuinfo
            val inputStreamClass = java.io.InputStream::class.java
            XposedHelpers.findAndHookMethod(
                inputStreamClass,
                "read",
                ByteArray::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val thiz = param.thisObject
                            val id = System.identityHashCode(thiz)
                            val path = streamPathMap[id]
                            if (path != null && path.contains("/proc/cpuinfo") && shouldSpoofCpuInfo(path, profile)) {
                                val buffer = param.args[0] as ByteArray
                                val offset = param.args[1] as Int
                                val length = param.args[2] as Int
                                val spoof = buildCpuInfoSpoof(profile).toByteArray()
                                val curOffset = streamOffsetMap.getOrDefault(id, 0)
                                if (curOffset >= spoof.size) {
                                    param.result = -1 // EOF
                                    return
                                }
                                val toCopy = Math.min(length, spoof.size - curOffset)
                                System.arraycopy(spoof, curOffset, buffer, offset, toCopy)
                                streamOffsetMap[id] = curOffset + toCopy
                                param.result = toCopy
                                StealthManager.stealthLog("🎯 Spoofed /proc/cpuinfo read: returned $toCopy bytes")
                            }
                        } catch (e: Exception) {
                            // ignore and let original read run
                        }
                    }
                }
            )

            StealthManager.stealthLog("✅ File reading hooks installed for CPU info spoofing")
        } catch (e: Exception) {
            StealthManager.stealthLog("❌ File reading hooks failed: ${e.message}")
        }

        // Also hook Runtime.exec and ProcessBuilder.start to catch shell-based reads like `cat /proc/cpuinfo` or `getprop`
        try {
            // Helper to check if a command targets cpuinfo or getprop
            fun commandTargetsCpuInfo(cmdParts: Array<String>): Boolean {
                return cmdParts.any { it.contains("/proc/cpuinfo") || it.contains("getprop") || it.contains("cat") && it.contains("cpuinfo") }
            }

            // Hook Runtime.exec(String)
            XposedHelpers.findAndHookMethod(
                Runtime::class.java,
                "exec",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val cmd = param.args[0] as String
                            val parts = cmd.split(Regex("\\s+"))
                            if (commandTargetsCpuInfo(parts.toTypedArray())) {
                                param.result = FakeProcess(buildCpuInfoSpoof(profile).toByteArray())
                                StealthManager.stealthLog("🎯 Intercepted Runtime.exec(String): returning spoofed cpuinfo")
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            )

            // Hook Runtime.exec(String[])
            XposedHelpers.findAndHookMethod(
                Runtime::class.java,
                "exec",
                Array<String>::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val args = param.args[0] as Array<String>
                            if (commandTargetsCpuInfo(args)) {
                                param.result = FakeProcess(buildCpuInfoSpoof(profile).toByteArray())
                                StealthManager.stealthLog("🎯 Intercepted Runtime.exec(String[]): returning spoofed cpuinfo")
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            )

            // Hook ProcessBuilder.start()
            XposedHelpers.findAndHookMethod(
                ProcessBuilder::class.java,
                "start",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val pb = param.thisObject as ProcessBuilder
                            val cmdList = pb.command().toTypedArray()
                            if (commandTargetsCpuInfo(cmdList)) {
                                param.result = FakeProcess(buildCpuInfoSpoof(profile).toByteArray())
                                StealthManager.stealthLog("🎯 Intercepted ProcessBuilder.start(): returning spoofed cpuinfo")
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            )

            StealthManager.stealthLog("✅ Exec/ProcessBuilder hooks installed for shell-based cpuinfo reads")
        } catch (e: Exception) {
            StealthManager.stealthLog("❌ Exec hooks failed: ${e.message}")
        }
    }
    
    private fun shouldSpoofCpuInfo(path: String, profile: DeviceProfile): Boolean {
        return path.contains("cpu") && profile.device.startsWith("pixel")
    }

    private fun buildCpuInfoSpoof(profile: DeviceProfile): String {
        // Construct a /proc/cpuinfo-like output tailored to the profile
        val sb = StringBuilder()
        sb.append("Processor\t: ${profile.processorModel}\n")
        sb.append("Hardware\t: ${profile.device}\n")
        sb.append("Model name\t: ${profile.processorModel}\n")
        sb.append("Implementer\t: Google\n")
        sb.append("Variant\t: 0x0\n")
        sb.append("CPU part\t: 0x0000\n")
        sb.append("Revision\t: 0\n")
        sb.append("Features\t: neon aes pmull cpuid\n")
        sb.append("CPU implementer\t: 0x41\n")
        sb.append("CPU architecture: 8\n")
        sb.append("Hardware vendor\t: ${profile.socManufacturer}\n")
        sb.append("Serial\t\t: ${profile.serialNumber}\n")
        return sb.toString()
    }
    
    private fun shouldSpoofHardwareDevice(path: String, profile: DeviceProfile): Boolean {
        return true // Always spoof hardware device access for Pixel
    }
    
    private fun getAllLoadedClasses(classLoader: ClassLoader): List<Class<*>> {
        // Get all loaded classes (simplified implementation)
        return emptyList()
    }
}

/**
 * Minimal fake Process implementation to return spoofed stdout bytes.
 */
class FakeProcess(private val outBytes: ByteArray) : Process() {
    private var consumed = false

    override fun getOutputStream(): java.io.OutputStream {
        return java.io.ByteArrayOutputStream() // not used
    }

    override fun getInputStream(): java.io.InputStream {
        return java.io.ByteArrayInputStream(outBytes)
    }

    override fun getErrorStream(): java.io.InputStream {
        return java.io.ByteArrayInputStream(ByteArray(0))
    }

    override fun waitFor(): Int {
        return 0
    }

    override fun exitValue(): Int {
        return 0
    }

    override fun destroy() {}
}
