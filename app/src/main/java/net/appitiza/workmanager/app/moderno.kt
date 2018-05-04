package net.appitiza.workmanager.app

import android.app.Application
import android.support.multidex.MultiDexApplication
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.utils.PreferenceHelper

class Moderno : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(this, Constants.PREF_NAME)
    }
}