package com.kashi.caimanspoof;

import android.util.Log;

public class NativeBridge {
    static {
        try {
            System.loadLibrary("pixelspoofer");
            Log.i("PixelSpoof", "Native lib loaded");
        } catch (UnsatisfiedLinkError e) {
            Log.e("PixelSpoof", "Failed to load native lib: " + e.getMessage());
        }
    }

    // Initialize native hooks for this process; returns true on success.
    public static native boolean initNativeHooks(String socModel, String socManufacturer);
}
