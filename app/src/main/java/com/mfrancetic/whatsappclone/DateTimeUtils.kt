package com.mfrancetic.whatsappclone

import java.util.*

class DateTimeUtils {

    companion object {

        fun getCurrentTime(): String {
            return Calendar.getInstance().time.toString()
        }

        fun formatTime(time: String): String {
            val year = time.substring(28)
            val date = time.replace("[TZ]", " ").substring(0, 9).plus(year)
            val timeFormatted = time.substring(11, 19)
            return date.plus("; ").plus(timeFormatted)
        }
    }
}