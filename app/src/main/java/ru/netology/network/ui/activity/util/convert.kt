package ru.netology.network.ui.activity.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
fun convertString2DateTime2String(dateString: String): String {
    val string2date = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(dateString)))
    val date2string = SimpleDateFormat("dd MMMM yyyy HH:mm").format(string2date)
    return date2string
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertString2Date2String(dateString: String): String {
    val string2date = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(dateString)))
    val date2string = SimpleDateFormat("dd MMMM yyyy").format(string2date)
    return date2string
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertDateTime2ISO_Instant(date: String, time: String): String {
    val string2date = SimpleDateFormat("dd.MM.yyyy HH:mm").parse("$date $time")
    val date2string = string2date.toInstant().toString()
    return date2string
}