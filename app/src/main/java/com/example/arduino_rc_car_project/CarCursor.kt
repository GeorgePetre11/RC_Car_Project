package com.example.arduino_rc_car_project.provider

import android.database.AbstractCursor

class CarCursor : AbstractCursor() {

    private val data = mutableListOf<Map<String, Any?>>()

    /**
     * Sets the data for the cursor.
     * @param cars A list of maps where each map represents a car with keys like "name", "model", and "ip".
     */
    fun setData(cars: List<Map<String, Any?>>) {
        data.clear()
        data.addAll(cars)
        // If you're using this cursor with UI components, make sure to rebind or refresh the UI
    }


    /**
     * Returns the column names for the cursor.
     */
    override fun getColumnNames(): Array<String> {
        return arrayOf("ip", "name", "model", "userId") // Ensure "ip" is included here
    }


    /**
     * Returns the number of rows in the cursor.
     */
    override fun getCount(): Int {
        return data.size
    }

    /**
     * Returns the value of the current row at the given column index as a String.
     * @param column The column index.
     */
    override fun getString(column: Int): String? {
        val key = getColumnNames()[column]
        return data[position][key]?.toString()
    }

    /**
     * Returns the value of the current row at the given column index as a Double.
     * @param column The column index.
     */
    override fun getDouble(column: Int): Double {
        val value = getString(column)
        return value?.toDoubleOrNull() ?: 0.0
    }

    /**
     * Returns the value of the current row at the given column index as a Float.
     * @param column The column index.
     */
    override fun getFloat(column: Int): Float {
        val value = getString(column)
        return value?.toFloatOrNull() ?: 0f
    }

    /**
     * Returns the value of the current row at the given column index as an Int.
     * @param column The column index.
     */
    override fun getInt(column: Int): Int {
        val value = getString(column)
        return value?.toIntOrNull() ?: 0
    }

    /**
     * Returns the value of the current row at the given column index as a Long.
     * @param column The column index.
     */
    override fun getLong(column: Int): Long {
        val value = getString(column)
        return value?.toLongOrNull() ?: 0L
    }

    /**
     * Returns the value of the current row at the given column index as a Short.
     * @param column The column index.
     */
    override fun getShort(column: Int): Short {
        val value = getString(column)
        return value?.toShortOrNull() ?: 0
    }

    /**
     * Checks if the value at the given column index is null.
     * @param column The column index.
     */
    override fun isNull(column: Int): Boolean {
        val key = getColumnNames()[column]
        return data[position][key] == null
    }
}
