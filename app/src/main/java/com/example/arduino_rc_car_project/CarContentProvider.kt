package com.example.arduino_rc_car_project.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore

class CarContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.arduino_rc_car_project.provider"
        const val CARS_TABLE = "cars"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$CARS_TABLE")

        private const val CODE_CARS_DIR = 1
        private const val CODE_CARS_ITEM = 2

        private val uriMatcher = android.content.UriMatcher(android.content.UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, CARS_TABLE, CODE_CARS_DIR)       // content://com.example.arduino_rc_car_project.provider/cars
            addURI(AUTHORITY, "$CARS_TABLE/*", CODE_CARS_ITEM) // content://com.example.arduino_rc_car_project.provider/cars/{carId}
        }
    }

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(): Boolean {
        firestore = FirebaseFirestore.getInstance()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val match = uriMatcher.match(uri)
        println("Query URI: $uri")
        println("Match: $match")

        return when (match) {
            CODE_CARS_DIR -> {
                // Query for all cars
                val carsCursor = CarCursor()
                firestore.collection("cars")
                    .get()
                    .addOnSuccessListener { result ->
                        val cars = result.documents.map { doc ->
                            mapOf(
                                "ip" to doc.getString("ip").orEmpty(),
                                "name" to doc.getString("name").orEmpty(),
                                "model" to doc.getString("model").orEmpty(),
                                "userId" to doc.getString("userId").orEmpty()
                            )
                        }
                        carsCursor.setData(cars)
                    }
                    .addOnFailureListener { e ->
                        println("Firestore query failed: ${e.message}")
                    }
                carsCursor
            }
            CODE_CARS_ITEM -> {
                // Query for a single car using 'name'
                val carName = uri.lastPathSegment.orEmpty()
                val carsCursor = CarCursor()
                firestore.collection("cars")
                    .whereEqualTo("name", carName)
                    .get()
                    .addOnSuccessListener { result ->
                        val cars = result.documents.map { doc ->
                            mapOf(
                                "ip" to doc.getString("ip").orEmpty(),
                                "name" to doc.getString("name").orEmpty(),
                                "model" to doc.getString("model").orEmpty(),
                                "userId" to doc.getString("userId").orEmpty()
                            )
                        }
                        carsCursor.setData(cars)
                    }
                    .addOnFailureListener { e ->
                        println("Firestore query failed: ${e.message}")
                    }
                carsCursor
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }



    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // No changes needed for insertion
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // Handle deletion
        val match = uriMatcher.match(uri)
        if (match == CODE_CARS_ITEM) {
            val carId = uri.lastPathSegment ?: return 0
            firestore.collection(CARS_TABLE).document(carId).delete()
            return 1
        }
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Update not supported")
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_CARS_DIR -> "vnd.android.cursor.dir/$AUTHORITY.$CARS_TABLE"
            CODE_CARS_ITEM -> "vnd.android.cursor.item/$AUTHORITY.$CARS_TABLE"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}
