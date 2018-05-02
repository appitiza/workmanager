package net.appitiza.workmanager.ui.activities.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.ComponentName
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.content.Context.ACTIVITY_SERVICE
import org.json.JSONObject
import android.content.Intent
import org.json.JSONException
import android.text.TextUtils
import android.support.v4.content.LocalBroadcastManager
import net.appitiza.workmanager.ui.activities.users.UserNotificationsActivity


class NotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (remoteMessage == null)
            return

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            handleNotification(remoteMessage.notification!!.body)
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {

            try {
                val json = JSONObject(remoteMessage.data.toString())
                handleDataMessage(json)
            } catch (e: Exception) {
            }

        }
    }

    private fun handleNotification(message: String?) {
        val notificationUtils = NotificationUtils(applicationContext)
        if (!notificationUtils.isAppIsInBackground(applicationContext)) {
            // app is in foreground, broadcast the push message
            val pushNotification = Intent("pushNotification")
            pushNotification.putExtra("message", message)
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

            // play notification sound
            val notificationUtils = NotificationUtils(applicationContext)
            notificationUtils.playNotificationSound()
        } else {
            // If the app is in background, firebase itself handles the notification
        }
    }

    private fun handleDataMessage(json: JSONObject) {

        try {
            val data = json.getJSONObject("data")

            val title = data.getString("title")
            val message = data.getString("message")
            val isBackground = data.getBoolean("is_background")
            val imageUrl = data.getString("image")
            val timestamp = data.getString("timestamp")
            val payload = data.getJSONObject("payload")


            val notificationUtils  = NotificationUtils(applicationContext)
            if (!notificationUtils.isAppIsInBackground(applicationContext)) {
                // app is in foreground, broadcast the push message
                val pushNotification = Intent("pushNotification")
                pushNotification.putExtra("message", message)
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

                // play notification sound
                val notificationUtils = NotificationUtils(applicationContext)
                notificationUtils.playNotificationSound()
            } else {
                // app is in background, show the notification in notification tray
                val resultIntent = Intent(applicationContext, UserNotificationsActivity::class.java)
                resultIntent.putExtra("message", message)

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(applicationContext, title, message, timestamp, resultIntent)
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(applicationContext, title, message, timestamp, resultIntent, imageUrl)
                }
            }
        } catch (e: JSONException) {
        } catch (e: Exception) {
        }

    }

    /**
     * Showing notification with text only
     */
    private fun showNotificationMessage(context: Context, title: String, message: String, timeStamp: String, intent: Intent) {
        val notificationUtils  = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent)
    }

    /**
     * Showing notification with text and image
     */
    private fun showNotificationMessageWithBigImage(context: Context, title: String, message: String, timeStamp: String, intent: Intent, imageUrl: String) {
        val notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl)
    }

}