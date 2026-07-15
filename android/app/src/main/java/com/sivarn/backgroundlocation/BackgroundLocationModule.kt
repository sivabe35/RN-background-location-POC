package com.sivarn.backgroundlocation

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

class BackgroundLocationModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val ACTION = "com.sivarn.backgroundlocation.BACKGROUND_LOCATION"
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val lat = intent?.getDoubleExtra("latitude", 0.0)
            val lng = intent?.getDoubleExtra("longitude", 0.0)
            val accuracy = intent?.getFloatExtra("accuracy", 0f)
            val payload = mapOf(
                "latitude" to lat,
                "longitude" to lng,
                "accuracy" to accuracy
            )
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("BackgroundLocation", payload)
        }
    }

    override fun getName(): String = "BackgroundLocationModule"

    @ReactMethod
    fun startTracking(options: ReadableMap?) {
        val intent = Intent(reactContext, BackgroundLocationService::class.java)
        intent.action = "START"
        val interval = options?.getInt("intervalMs") ?: 15000
        intent.putExtra("intervalMs", interval.toLong())
        reactContext.startForegroundService(intent)
        // register receiver
        reactContext.registerReceiver(receiver, IntentFilter(ACTION))
    }

    @ReactMethod
    fun stopTracking() {
        val intent = Intent(reactContext, BackgroundLocationService::class.java)
        intent.action = "STOP"
        reactContext.startService(intent)
        try {
            reactContext.unregisterReceiver(receiver)
        } catch (e: Exception) { }
    }

    @ReactMethod
    fun updateInterval(options: ReadableMap?) {
        val interval = options?.getInt("intervalMs") ?: return
        val intent = Intent(reactContext, BackgroundLocationService::class.java)
        intent.action = "UPDATE_INTERVAL"
        intent.putExtra("intervalMs", interval.toLong())
        reactContext.startService(intent)
    }
}
