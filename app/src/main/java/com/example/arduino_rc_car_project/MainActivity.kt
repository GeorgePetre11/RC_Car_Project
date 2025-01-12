package com.example.arduino_rc_car_project

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.arduino_rc_car_project.ui.Authentication
import com.example.arduino_rc_car_project.ui.CarDiscoveryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import kotlin.concurrent.thread
import android.content.SharedPreferences
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var carListView: ListView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val carList = mutableListOf<String>()
    private val carIdList = mutableListOf<String>()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("RC_Car_Preferences", Context.MODE_PRIVATE)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        welcomeTextView = findViewById(R.id.welcomeTextView)
        carListView = findViewById(R.id.carListView)

        loadUserCars()

        val addTestCarButton = findViewById<Button>(R.id.addTestCarButton)

        addTestCarButton.setOnClickListener {
            addTestCarToFirestore()
        }

        val addCarButton = findViewById<Button>(R.id.addCarButton)
        addCarButton.setOnClickListener {
            val intent = Intent(this, CarDiscoveryActivity::class.java)
            startActivity(intent)
        }

        // Refresh Button
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            loadUserCars()
        }

        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener { view ->
            showSettingsPopup(view)
        }



        val user = FirebaseAuth.getInstance().currentUser
        welcomeTextView.text = "Hello, ${user?.email ?: "User"}"
    }

    // Load user's cars from Firestore
    private fun loadUserCars() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("cars")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                carList.clear()
                carIdList.clear()

                for (document in result) {
                    val carName = document.getString("name") ?: "Unnamed Car"
                    carList.add(carName)
                    carIdList.add(document.id)
                }

                val adapter = CarListAdapter(this, carList, carIdList)
                carListView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load cars", Toast.LENGTH_SHORT).show()
            }
    }

    // Add a test car to Firestore
    private fun addTestCarToFirestore() {
        val userId = auth.currentUser?.uid ?: return

        val carData = hashMapOf(
            "name" to "Test RC Car",
            "model" to "Arduino",
            "userId" to userId
        )

        db.collection("cars").add(carData)
            .addOnSuccessListener {
                Toast.makeText(this, "Car added successfully!", Toast.LENGTH_SHORT).show()
                loadUserCars()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add car", Toast.LENGTH_SHORT).show()
            }
    }

    // Network scanning for ESP8266 RC Cars
    private fun scanNetworkForCar() {
        val devices = mutableListOf<String>()
        val localIp = getLocalIpAddress()
        val subnet = localIp.substring(0, localIp.lastIndexOf('.'))

        thread {
            for (i in 1..254) {
                val testIp = "$subnet.$i"
                try {
                    val address = InetAddress.getByName(testIp)
                    if (address.isReachable(200)) {
                        val url = URL("http://$testIp/identify")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 1000
                        connection.readTimeout = 1000

                        if (connection.responseCode == 200) {
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            if (response.contains("RC_Car_ESP8266")) {
                                devices.add(testIp)
                                runOnUiThread {
                                    Toast.makeText(this, "Car Found: $testIp", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Skip unreachable IPs
                }
            }
            runOnUiThread {
                if (devices.isEmpty()) {
                    Toast.makeText(this, "No Cars Detected", Toast.LENGTH_SHORT).show()
                } else {
                    showDeviceList(devices)
                }
            }
        }
    }

    // Display available cars and add selected one to Firebase
    private fun showDeviceList(devices: List<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Device to Add")
        builder.setItems(devices.toTypedArray()) { _, which ->
            val selectedIp = devices[which]
            addCarToFirebase(selectedIp)
        }
        builder.show()
    }

    // Add the selected car to Firebase
    private fun addCarToFirebase(ip: String) {
        val userId = auth.currentUser?.uid ?: return

        val carData = hashMapOf(
            "name" to "Discovered RC Car",
            "ipAddress" to ip,
            "model" to "Arduino",
            "userId" to userId
        )

        db.collection("cars").add(carData)
            .addOnSuccessListener {
                Toast.makeText(this, "Car Added!", Toast.LENGTH_SHORT).show()
                loadUserCars()  // Reload cars to show the new one
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add car", Toast.LENGTH_SHORT).show()
            }
    }

    // Get the local IP of the Android device
    private fun getLocalIpAddress(): String {
        NetworkInterface.getNetworkInterfaces().toList().forEach { intf ->
            intf.inetAddresses.toList().forEach { addr ->
                if (!addr.isLoopbackAddress && addr is InetAddress) {
                    return addr.hostAddress ?: ""
                }
            }
        }
        return ""
    }

    private fun showSettingsPopup(anchorView: View) {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_settings, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Initialize Popup Elements
        val darkModeSwitch = popupView.findViewById<Switch>(R.id.darkModeSwitch)
        val logoutButton = popupView.findViewById<Button>(R.id.logoutButton)
        val exitAppButton = popupView.findViewById<Button>(R.id.exitAppButton)

        // Load saved dark mode preference
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        darkModeSwitch.isChecked = isDarkMode

        // Handle Dark Mode Switch
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference("DarkMode", isChecked)
            applyDarkMode(isChecked)
        }

        // Handle Logout Button
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, Authentication::class.java))
            finish()
            popupWindow.dismiss()
        }

        // Handle Exit App Button
        exitAppButton.setOnClickListener {
            finishAffinity() // Closes all activities and exits the app
        }

        // Show the popup
        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    private fun savePreference(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

}
