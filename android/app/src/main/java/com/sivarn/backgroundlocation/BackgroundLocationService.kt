package com.sivarn.backgroundlocation

import android.app.Service
import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.app.NotificationManager.IMPORTANCE_LOW
import com.google.android.gms.location.*
import android.util.Log

class BackgroundLocationService : Service() {
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var intervalMs: Long = 15000
    private var channelId = "background_location_channel"

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    sendLocationToReact(location)
                }
            }
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Background Location"
            val channel = NotificationChannel(channelId, name, IMPORTANCE_LOW)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, BackgroundLocationService::class.java).apply { action = "STOP" }
        val pendingStop = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }
        builder.setContentTitle("Background Location")
            .setContentText("Tracking location in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .addAction(android.R.drawable.ic_delete, "Stop", pendingStop)
        return builder.build()
    }

    private fun sendLocationToReact(location: android.location.Location) {
        val intent = Intent("com.sivarn.backgroundlocation.BACKGROUND_LOCATION")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("accuracy", location.accuracy)
        sendBroadcast(intent)
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.create().apply {
            interval = intervalMs
            fastestInterval = intervalMs / 2
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        fusedClient.requestLocationUpdates(request, locationCallback, mainLooper)
        startForeground(101, buildNotification())
    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "START" -> {
                intervalMs = intent?.getLongExtra("intervalMs", intervalMs) ?: intervalMs
                startLocationUpdates()
            }
            "UPDATE_INTERVAL" -> {
                intervalMs = intent?.getLongExtra("intervalMs", intervalMs) ?: intervalMs
                stopLocationUpdates()
                startLocationUpdates()
            }
            "STOP" -> {
                stopLocationUpdates()
            }
            else -> {
                // default: start
                intervalMs = intent?.getLongExtra("intervalMs", intervalMs) ?: intervalMs
                startLocationUpdates()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
