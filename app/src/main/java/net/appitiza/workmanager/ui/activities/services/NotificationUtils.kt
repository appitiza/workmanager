package net.appitiza.workmanager.ui.activities.services

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import net.appitiza.workmanager.R
import java.text.ParseException
import java.text.SimpleDateFormat


class NotificationUtils(private var mContext: Context){
    private val TAG = NotificationUtils::class.java.simpleName

    fun showNotificationMessage(title: String, message: String, timeStamp: String, intent: Intent) {
        showNotificationMessage(title, message, timeStamp, intent, null)
    }

    fun showNotificationMessage(title: String, message: String, timeStamp: String, intent: Intent, imageUrl: String?) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return


        // notification icon
        val icon = R.mipmap.ic_launcher

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        )

        val mBuilder = NotificationCompat.Builder(
                mContext)

        val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + mContext.packageName+ "/raw/notification")

        showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
        playNotificationSound()
    }

    private fun showSmallNotification(mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String, timeStamp: String, resultPendingIntent: PendingIntent, alarmSound: Uri) {

        val inboxStyle = NotificationCompat.InboxStyle()

        inboxStyle.addLine(message)

        val notification: Notification
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(inboxStyle)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
                .setContentText(message)
                .build()

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(100, notification)
    }


    // Playing notification sound
    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.packageName + "/raw/notification")
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Method checks if the app is in background or not
     */
    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo.packageName == context.packageName) {
                isInBackground = false
            }
        }

        return isInBackground
    }

    // Clears notification tray messages
    fun clearNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun getTimeMilliSec(timeStamp: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val date = format.parse(timeStamp)
            return date.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return 0
    }
}