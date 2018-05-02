package net.appitiza.workmanager.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import net.appitiza.workmanager.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit




class Utils {
    companion object {


        fun showDialog(mContext : Context,message:String )
        {
            val mdialog: AlertDialog = AlertDialog.Builder(mContext).create()
            mdialog.setTitle(mContext.getString(R.string.app_name))
            mdialog.setMessage(message)
            mdialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.ok), DialogInterface.OnClickListener { dialog, which -> mdialog.dismiss() })
            mdialog.show()
        }
        fun convertDate(milli: Long, dateFormat: String): String {
            val format = SimpleDateFormat(dateFormat, Locale.ENGLISH)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = milli
            val value = format.format(calendar.time)
            return value
        }

        fun convertHours(millis: Long): String {
            val hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
            return hms
        }
        fun convertDays(startmillis: Long,stopmillis: Long): Long {
            val msDiff = stopmillis - startmillis
            val daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
            return daysDiff
        }

        fun isWithinRange(centerLatitude: Double, centerLongitude: Double, testLatitude: Double, testLongitude: Double, range: Float): Boolean {
            val results = FloatArray(1)
            Location.distanceBetween(centerLatitude, centerLongitude, testLatitude, testLongitude, results)
            val distanceInMeters = results[0]
            return distanceInMeters < (range * 1000)
        }
        fun getDate(date: String,format: String): Date {
            val format = SimpleDateFormat(format,Locale.ENGLISH)
            val value: Date = format.parse(date)
            return value
        }
        fun getDateTimestamp(date: String): Date {
            val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val value: Date = format.parse(date)
            return value
        }


    }
}