#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "PixelSpoof", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "PixelSpoof", __VA_ARGS__)

// Simple wrapper to try to override __system_property_get by patching the GOT/PLT is complex
// and risky. For now, we implement a conservative approach: provide a hook function pointer
// that can be used by higher-level code. If we can't reliably patch, we return false.

extern "C" JNIEXPORT jboolean JNICALL
Java_com_kashi_caimanspoof_NativeBridge_initNativeHooks(JNIEnv *env, jclass clazz, jstring jSocModel, jstring jSocManu) {
    const char* socModel = env->GetStringUTFChars(jSocModel, nullptr);
    const char* socManu = env->GetStringUTFChars(jSocManu, nullptr);

    LOGI("initNativeHooks called - socModel=%s socManu=%s", socModel, socManu);

    // Try to locate __system_property_get in libc
    void* handle = dlopen("/system/lib64/libc.so", RTLD_NOW);
    if (!handle) {
        handle = dlopen("/system/lib/libc.so", RTLD_NOW);
    }
    if (!handle) {
        LOGE("Could not open libc to find __system_property_get: %s", dlerror());
        env->ReleaseStringUTFChars(jSocModel, socModel);
        env->ReleaseStringUTFChars(jSocManu, socManu);
        return JNI_FALSE;
    }

    void* sym = dlsym(handle, "__system_property_get");
    if (!sym) {
        LOGE("__system_property_get not found: %s", dlerror());
        dlclose(handle);
        env->ReleaseStringUTFChars(jSocModel, socModel);
        env->ReleaseStringUTFChars(jSocManu, socManu);
        return JNI_FALSE;
    }

    LOGI("Found __system_property_get at %p -- not patching automatically in this build (safe mode)", sym);

    // We won't attempt to patch at binary level in this commit to avoid crashes.
    // Returning true indicates native lib loaded and found symbol; later iterations
    // can implement a safe inline hook.

    dlclose(handle);
    env->ReleaseStringUTFChars(jSocModel, socModel);
    env->ReleaseStringUTFChars(jSocManu, socManu);

    return JNI_TRUE;
}
