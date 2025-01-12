package com.example.arduino_rc_car_project.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arduino_rc_car_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import kotlin.concurrent.thread

class CarDiscoveryActivity : AppCompatActivity() {

    private lateinit var deviceListView: ListView
    private lateinit var scanButton: Button
    private val deviceList = mutableListOf<String>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ArrayAdapter<String>
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_discovery)

        deviceListView = findViewById(R.id.deviceListView)
        scanButton = findViewById(R.id.scanButton)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        deviceListView.adapter = adapter

        // Start scan on button press
        scanButton.setOnClickListener {
            scanNetworkForDevices()
        }

        // Handle device selection to add to Firebase
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = deviceList[position]
            addCarToFirestore(selectedDevice)
        }
    }

    private fun scanNetworkForDevices() {
        Toast.makeText(this, "Scanning network...", Toast.LENGTH_SHORT).show()
        deviceList.clear()
        adapter.notifyDataSetChanged()

        progressDialog = ProgressDialog(this).apply {
            setMessage("Scanning network, please wait...")
            setCancelable(false)
            show()
        }

        thread {
            try {
                val subnet = getLocalIpAddress()?.substringBeforeLast('.') ?: return@thread
                for (i in 1..254) {
                    val host = "$subnet.$i"
                    try {
                        val address = InetAddress.getByName(host)

                        if (address.isReachable(100)) {
                            // Check if it’s an RC car by hitting the /identify endpoint
                            if (isCarDevice(address.hostAddress)) {
                                runOnUiThread {
                                    deviceList.add(address.hostAddress)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    } catch (e: IOException) {
                        // Skip unreachable IPs
                    }
                }
                runOnUiThread {
                    progressDialog?.dismiss()
                    if (deviceList.isEmpty()) {
                        Toast.makeText(this, "No RC cars found.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Scan complete.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressDialog?.dismiss()
                    Toast.makeText(this, "Scan failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isCarDevice(ip: String): Boolean {
        return try {
            val url = URL("http://$ip/identify")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 500
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode

            // Check for specific response code or message
            responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    private fun addCarToFirestore(deviceIp: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val carData = hashMapOf(
            "name" to "Discovered Car",
            "model" to "Arduino",
            "ip" to deviceIp,
            "userId" to userId
        )

        db.collection("cars")
            .add(carData)
            .addOnSuccessListener {
                Toast.makeText(this, "Car added: $deviceIp", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add car.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLocalIpAddress(): String? {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            val addrs = intf.inetAddresses
            for (addr in addrs) {
                if (!addr.isLoopbackAddress) {
                    val sAddr = addr.hostAddress
                    if (sAddr.indexOf(':') < 0) return sAddr
                }
            }
        }
        return null
    }
}
