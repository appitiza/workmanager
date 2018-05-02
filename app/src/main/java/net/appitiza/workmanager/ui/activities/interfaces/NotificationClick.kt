package net.appitiza.workmanager.ui.activities.interfaces

import net.appitiza.workmanager.model.NotificationData

interface NotificationClick {
    fun onClick(data:NotificationData)
}