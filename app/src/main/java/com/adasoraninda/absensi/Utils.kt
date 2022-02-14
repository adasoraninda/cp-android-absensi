package com.adasoraninda.absensi

import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun getCurrentDate(): String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MM-yyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(currentTime)
    }

}