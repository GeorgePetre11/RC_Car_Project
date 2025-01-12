package com.example.arduino_rc_car_project.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.arduino_rc_car_project.HeartbeatService
import com.example.arduino_rc_car_project.R
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class CarControlActivity : AppCompatActivity() {

    // Hardcoded IP Address
    private val carIpAddress = "192.168.0.179"
    private val channelId = "car_status_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_control)

        createNotificationChannel()

        // Example: Start Heartbeat Service
        val intent = Intent(this, HeartbeatService::class.java)
        startService(intent)

        // Set up button actions
        findViewById<Button>(R.id.forwardButton).setOnClickListener {
            sendCommand("/forward")
        }
        findViewById<Button>(R.id.backwardButton).setOnClickListener {
            sendCommand("/backward")
        }
        findViewById<Button>(R.id.leftButton).setOnClickListener {
            sendCommand("/left")
        }
        findViewById<Button>(R.id.rightButton).setOnClickListener {
            sendCommand("/right")
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            sendCommand("/stop")
        }
    }

    private fun sendCommand(command: String) {
        thread {
            try {
                val url = URL("http://$carIpAddress$command")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.responseCode // Trigger the command
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Car Status Channel"
            val descriptionText = "Channel for car connection status"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}