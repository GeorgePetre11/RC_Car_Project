package com.example.arduino_rc_car_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class HeartbeatService : Service() {

    private val carIpAddress = "http://192.168.0.179" // Replace with your car's IP
    private val channelId = "car_status_channel"
    private var isConnected = true // Track connection status

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Start the service in the foreground
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your app's icon
            .setContentTitle("Monitoring Car Status")
            .setContentText("Heartbeat monitor is active.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        // Start monitoring the car's heartbeat
        startHeartbeatMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Keep the service running unless explicitly stopped
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // No binding for this service
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Heartbeat Service Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun startHeartbeatMonitoring() {
        thread {
            while (true) {
                try {
                    val url = URL("$carIpAddress/heartbeat")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 3000 // 3 seconds timeout
                    connection.readTimeout = 3000

                    val responseCode = connection.responseCode

                    if (responseCode == 200) {
                        handleReconnection()
                    } else {
                        handleDisconnection()
                    }
                } catch (e: Exception) {
                    handleDisconnection()
                }

                Thread.sleep(5000) // Wait 5 seconds before the next heartbeat check
            }
        }
    }

    private fun handleDisconnection() {
        if (isConnected) { // Notify only if the status changes
            sendNotification("RC Car Disconnected", "The car is out of range. Please check the connection.")
            sendStatusBroadcast(false) // Broadcast disconnection to the activity
            isConnected = false
        }
    }

    private fun handleReconnection() {
        if (!isConnected) { // Notify only if the status changes
            sendNotification("RC Car Reconnected", "The car is back online.")
            sendStatusBroadcast(true) // Broadcast reconnection to the activity
            isConnected = true
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification) // Use a unique ID for each notification
    }

    private fun sendStatusBroadcast(isConnected: Boolean) {
        val intent = Intent("com.example.arduino_rc_car_project.CAR_STATUS")
        intent.putExtra("isConnected", isConnected)
        sendBroadcast(intent) // Send a broadcast to inform the activity of the car's status
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Car Status Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about the car connection status"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
