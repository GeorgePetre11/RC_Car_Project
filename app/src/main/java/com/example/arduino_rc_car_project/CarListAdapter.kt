package com.example.arduino_rc_car_project

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.arduino_rc_car_project.ui.CarControlActivity
import com.google.firebase.firestore.FirebaseFirestore

class CarListAdapter(
    private val context: Context,
    private val cars: MutableList<String>, // Changed to MutableList to allow removal of items
    private val carIds: MutableList<String> // Changed to MutableList to allow removal of items
) : BaseAdapter() {

    override fun getCount(): Int {
        return cars.size
    }

    override fun getItem(position: Int): Any {
        return cars[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_car, parent, false)

        val carNameTextView = view.findViewById<TextView>(R.id.carNameTextView)
        val controlButton = view.findViewById<Button>(R.id.controlButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton) // Add reference to the delete button

        carNameTextView.text = cars[position]

        // Handle button click to navigate to CarControlActivity
        controlButton.setOnClickListener {
            val intent = Intent(context, CarControlActivity::class.java)
            // No IP fetching here, the IP address is hardcoded in CarControlActivity
            context.startActivity(intent)
        }


        // Handle delete button click
        deleteButton.setOnClickListener {
            deleteCarFromDatabase(position)
        }

        return view
    }

    private fun deleteCarFromDatabase(position: Int) {
        val db = FirebaseFirestore.getInstance()
        val carId = carIds[position]

        db.collection("cars").document(carId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Car deleted successfully.", Toast.LENGTH_SHORT).show()

                // Remove the item from the list and refresh the adapter
                cars.removeAt(position)
                carIds.removeAt(position)
                notifyDataSetChanged() // Update the ListView
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete car.", Toast.LENGTH_SHORT).show()
            }
    }
}
