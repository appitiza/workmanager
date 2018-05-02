package net.appitiza.workmanager.model

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

open class SiteListdata : Serializable{
     var siteid: String? = null
     var sitename: String? = null
     var location: GeoPoint? = null
     var lat: Double = 0.0
     var lon: Double = 0.0
     var type: String? = null
     var date: String = ""
     var cost: String? = null
     var contact: String? = null
     var person: String? = null
     var status: String? = null


}