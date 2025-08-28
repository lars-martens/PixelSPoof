package com.kashi.caimanspoof

import android.content.Context
import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Debug utility to test property spoofing
 */
class PropertyDebugger {
    
    companion object {
    // Map stream identity -> path for diagnostics when instrumenting specific apps
    private val streamPathMap: MutableMap<Int, String> = mutableMapOf()

        fun testPropertyAccess(lpparam: XC_LoadPackage.LoadPackageParam) {
            try {
                StealthManager.stealthLog("🔍 TESTING PROPERTY ACCESS...")
                
                // Test SystemProperties directly
                val systemPropertiesClass = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader)
                
                // Try to get some common properties
                val testProperties = listOf(
                    "ro.product.model",
                    "ro.product.brand", 
                    "ro.product.manufacturer",
                    "ro.product.device",
                    "ro.build.fingerprint",
                    "ro.build.id"
                )
                
                testProperties.forEach { prop ->
                    try {
                        val value = XposedHelpers.callStaticMethod(
                            systemPropertiesClass,
                            "get",
                            prop
                        ) as String
                        StealthManager.stealthLog("🧪 TEST: $prop = $value")
                    } catch (e: Exception) {
                        StealthManager.stealthLog("🧪 TEST FAILED: $prop - ${e.message}")
                    }
                }
                
                // Test Build class access
                StealthManager.stealthLog("🧪 Testing Build class...")
                StealthManager.stealthLog("🧪 Build.MODEL = ${Build.MODEL}")
                StealthManager.stealthLog("🧪 Build.BRAND = ${Build.BRAND}")
                StealthManager.stealthLog("🧪 Build.MANUFACTURER = ${Build.MANUFACTURER}")
                StealthManager.stealthLog("🧪 Build.DEVICE = ${Build.DEVICE}")
                StealthManager.stealthLog("🧪 Build.FINGERPRINT = ${Build.FINGERPRINT}")
                
            } catch (e: Exception) {
                StealthManager.stealthLog("🧪 Property test failed: ${e.message}")
            }
        }
        
        /**
         * Hook application context creation to run tests
         */
        fun setupTestHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
            try {
                // Hook Application onCreate to run our tests
                XposedHelpers.findAndHookMethod(
                    "android.app.Application",
                    lpparam.classLoader,
                    "onCreate",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            StealthManager.stealthLog("🧪 Application started, running property tests...")
                            testPropertyAccess(lpparam)
                        }
                    }
                )

                // If this is the specific device-info app, install deeper instrumentation
                val targetPkg = "ru.andr7e.deviceinfohw"
                if (lpparam.packageName == targetPkg) {
                    de.robv.android.xposed.XposedBridge.log("PixelSpoof: 🔧 Installing deep instrumentation for $targetPkg - hook entry reached")

                    try {
                        // Hook FileInputStream(File) (use system classloader)
                        XposedHelpers.findAndHookConstructor(
                            "java.io.FileInputStream",
                            null,
                            java.io.File::class.java,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    try {
                                        val f = param.args[0] as java.io.File
                                        val id = System.identityHashCode(param.thisObject)
                                        streamPathMap[id] = f.path
                                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] FileInputStream opened for: ${f.path}")
                                    } catch (e: Exception) {
                                        StealthManager.stealthLog("[instr] FileInputStream hook error: ${e.message}")
                                    }
                                }
                            }
                        )

                        // Hook FileInputStream(String)
                        XposedHelpers.findAndHookConstructor(
                            "java.io.FileInputStream",
                            null,
                            String::class.java,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    try {
                                        val path = param.args[0] as String
                                        val id = System.identityHashCode(param.thisObject)
                                        streamPathMap[id] = path
                                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] FileInputStream opened for: $path")
                                    } catch (e: Exception) {
                                        StealthManager.stealthLog("[instr] FileInputStream(String) hook error: ${e.message}")
                                    }
                                }
                            }
                        )

                        // Hook RandomAccessFile(File, String)
                        XposedHelpers.findAndHookConstructor(
                            "java.io.RandomAccessFile",
                            null,
                            java.io.File::class.java,
                            String::class.java,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    try {
                                        val f = param.args[0] as java.io.File
                                        val id = System.identityHashCode(param.thisObject)
                                        streamPathMap[id] = f.path
                                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] RandomAccessFile opened for: ${f.path}")
                                    } catch (e: Exception) {
                                        StealthManager.stealthLog("[instr] RandomAccessFile hook error: ${e.message}")
                                    }
                                }
                            }
                        )

                        // Hook InputStream.read(byte[], int, int) to log reads on tracked streams
                        XposedHelpers.findAndHookMethod(
                            "java.io.InputStream",
                            null,
                            "read",
                            ByteArray::class.java,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType,
                            object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    try {
                                        val id = System.identityHashCode(param.thisObject)
                                        val path = streamPathMap[id]
                                        if (path != null) {
                                            val len = param.args[2] as Int
                                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] InputStream.read on $path (len=$len)")
                                            val stack = Thread.currentThread().stackTrace
                                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] Stack trace:\n${stack.joinToString("\n")}")
                                        }
                                    } catch (e: Exception) {
                                        StealthManager.stealthLog("[instr] InputStream.read hook error: ${e.message}")
                                    }
                                }
                            }
                        )

                        // Hook android.os.SystemProperties.get(String) and get(String, String)
                        try {
                            val sysPropClass = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader)
                            XposedHelpers.findAndHookMethod(sysPropClass, "get", String::class.java, object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    try {
                                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] SystemProperties.get(${param.args[0]}) called")
                                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] Stack: ${Thread.currentThread().stackTrace.joinToString("\n")}")
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                }
                            })
                            XposedHelpers.findAndHookMethod(sysPropClass, "get", String::class.java, String::class.java, object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    try {
                                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] SystemProperties.get(${param.args[0]}, <default>) called")
                                    } catch (e: Exception) {
                                    }
                                }
                            })
                        } catch (e: Exception) {
                            StealthManager.stealthLog("[instr] SystemProperties hooks failed: ${e.message}")
                        }

                        // Hook Runtime.exec variants (common overloads)
                        try {
                            XposedHelpers.findAndHookMethod(
                                "java.lang.Runtime",
                                null,
                                "exec",
                                String::class.java,
                                object : XC_MethodHook() {
                                    override fun beforeHookedMethod(param: MethodHookParam) {
                                        try {
                                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] Runtime.exec(String) -> ${param.args[0]}")
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                            )

                            XposedHelpers.findAndHookMethod(
                                "java.lang.Runtime",
                                null,
                                "exec",
                                Array<String>::class.java,
                                object : XC_MethodHook() {
                                    override fun beforeHookedMethod(param: MethodHookParam) {
                                        try {
                                            val cmd = (param.args[0] as Array<String>).joinToString(" ")
                                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] Runtime.exec(String[]) -> $cmd")
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            StealthManager.stealthLog("[instr] Runtime.exec hooks failed: ${e.message}")
                        }

                        // Hook ProcessBuilder.start()
                        try {
                            XposedHelpers.findAndHookMethod(
                                "java.lang.ProcessBuilder",
                                null,
                                "start",
                                object : XC_MethodHook() {
                                    override fun beforeHookedMethod(param: MethodHookParam) {
                                        try {
                                            val pb = param.thisObject
                                            // Try to reflectively read the command list
                                            val cmdField = pb::class.java.getMethod("command")
                                            val cmdObj = cmdField.invoke(pb)
                                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] ProcessBuilder.start() -> $cmdObj")
                                        } catch (e: Exception) {
                                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [instr] ProcessBuilder.start hook error: ${e.message}")
                                        }
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            StealthManager.stealthLog("[instr] ProcessBuilder.start hook failed: ${e.message}")
                        }

                        } catch (e: Exception) {
                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: ❌ Failed to install deep instrumentation: ${e.message}")
                    }
                    // Immediate in-process synchronous probe to read /proc/cpuinfo and SystemProperties.
                    try {
                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] immediate probing from inside $targetPkg")
                        try {
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] Reading /proc/cpuinfo via java.io.FileInputStream (sync)")
                            val fis = java.io.FileInputStream("/proc/cpuinfo")
                            val content = fis.bufferedReader().use { it.readText() }
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] /proc/cpuinfo (java) -> ${content.take(512)}")
                        } catch (e: Exception) {
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] java read error: ${e.message}")
                        }

                        try {
                            val sysPropClass = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader)
                            val soc = XposedHelpers.callStaticMethod(sysPropClass, "get", "ro.soc.model") as String
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] SystemProperties.get(ro.soc.model) -> $soc")
                        } catch (e: Exception) {
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] SystemProperties.get error: ${e.message}")
                        }

                        try {
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] Running exec cat /proc/cpuinfo (sync)")
                            val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", "cat /proc/cpuinfo"))
                            val out = p.inputStream.bufferedReader().use { it.readText() }
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] /proc/cpuinfo (exec) -> ${out.take(512)}")
                        } catch (e: Exception) {
                            de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] exec read error: ${e.message}")
                        }
                    } catch (e: Exception) {
                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: [probe] immediate probe error: ${e.message}")
                    }

                    // Attempt to load native hooks for this process (safe-mode)
                    try {
                        val ok = com.kashi.caimanspoof.NativeBridge.initNativeHooks("Tensor G5", "Google")
                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: NativeBridge.initNativeHooks returned: $ok")
                    } catch (e: Exception) {
                        de.robv.android.xposed.XposedBridge.log("PixelSpoof: Native bridge load failed: ${e.message}")
                    }
                }
                de.robv.android.xposed.XposedBridge.log("PixelSpoof: ✅ Debug hooks installed")
                
            } catch (e: Exception) {
                StealthManager.stealthLog("❌ Debug hook setup failed: ${e.message}")
            }
        }
    }
}
