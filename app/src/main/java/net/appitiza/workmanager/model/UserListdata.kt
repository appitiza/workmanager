package net.appitiza.workmanager.model

import java.io.Serializable

open class UserListdata : Serializable {
    var username: String? = null
    var emailId: String? = null
    var status: String? = null
    var token: String? = null
    var imei: String? = null
    var salary: Int = 0
    var type: String? = null
    var image: String? = null
    var thumb: String? = null
    var seen: Long = 0
    var time: Long = 0


}