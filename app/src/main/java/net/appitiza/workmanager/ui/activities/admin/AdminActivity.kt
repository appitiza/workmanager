package net.appitiza.workmanager.ui.activities.admin

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.activity_profile.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.StartUpActivity
import net.appitiza.workmanager.utils.PreferenceHelper
import java.util.*

class AdminActivity : AppCompatActivity() {
    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var userimei by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_IMEI, "")
    private var userimage by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_IMAGE, "")
    private var userthumb by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_THUMB, "")
    private var userstatus by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_STATUS, "")
    private var salary by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_SALARY, 0)

    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        initialize()
        getUserData()
        setclick()
    }

    private fun initialize() {
        db = FirebaseFirestore.getInstance()
        updateFcm()
    }
    private fun getUserData() {
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.default_image)
        requestOptions.error(R.drawable.default_image)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.circleCrop()
        Glide.with(applicationContext).load(userimage).apply(requestOptions).into(iv_admin_image)
        tv_admin_displayname.text = displayName
        tv_admin_email.text = useremail
        tv_admin_status.text = userstatus


    }
    private fun setclick() {
        ll_admin_home_sites.setOnClickListener { loadSites() }
        ll_admin_home_site_reports.setOnClickListener { loadSitesReport() }
        ll_admin_home_wrk_reports.setOnClickListener { loadWorkReport() }
        ll_admin_home_site_notification.setOnClickListener { loadNotification() }
        ll_admin_home_site_adjust_time.setOnClickListener { loadsettime() }
        ll_admin_home_site_device.setOnClickListener { loadDeviceReset() }
        ll_admin_home_site_salary.setOnClickListener { loadSetsalary() }
        ll_admin_home_change_device_resquests.setOnClickListener { loadDeviceChangeRequests() }
        ll_admin_home_site_chat.setOnClickListener { loadChatUser() }
        ll_admin_home_profile.setOnClickListener { loadProfile() }

    }

    private fun loadSites() {
        val intent = Intent(this@AdminActivity, AdminSitesActivity::class.java)

        val p1 = Pair(tv_admin_home_sites as View, getString(R.string.txt_adminhome_sites))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    private fun loadSitesReport() {
        val intent = Intent(this@AdminActivity, AdminSiteReportsActivity::class.java)

        val p1 = Pair(tv_admin_home_site_reports as View, getString(R.string.txt_adminhome_sitesreport))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    private fun loadWorkReport() {
        val intent = Intent(this@AdminActivity, AdminWorkReportsActivity::class.java)

        val p1 = Pair(tv_admin_home_wrk_reports as View, getString(R.string.txt_adminhome_wrkreport))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    private fun loadNotification() {

        val intent = Intent(this@AdminActivity, NotificationActivity::class.java)
        val p1 = Pair(tv_admin_home_site_notification as View, getString(R.string.txt_adminhome_notification))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    private fun loadSetsalary() {

        val intent = Intent(this@AdminActivity, SetSalaryActivity::class.java)
        val p1 = Pair(tv_admin_home_site_salary as View, getString(R.string.txt_adminhome_salary))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    private fun loadDeviceReset() {

        val intent = Intent(this@AdminActivity, DeviceResetActivity::class.java)
        val p1 = Pair(tv_admin_home_site_device as View, getString(R.string.txt_adminhome_device))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    private fun loadsettime() {

        val intent = Intent(this@AdminActivity, SetTimeActivity::class.java)
        val p1 = Pair(tv_admin_home_site_adjust_time as View, getString(R.string.txt_adminhome_adjust_time))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    fun loadDeviceChangeRequests() {

        val intent = Intent(this@AdminActivity, DeviceChangeRequestsActivity::class.java)
        val p1 = Pair(tv_admin_home_change_device_resquests as View, getString(R.string.txt_adminhome_device_change_request))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    fun loadProfile() {

        val intent = Intent(this@AdminActivity, ProfileActivity::class.java)
        val p1 = Pair(tv_admin_home_profile as View, getString(R.string.txt_adminhome_device_change_request))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    fun loadChatUser() {

        val intent = Intent(this@AdminActivity, UserListActivity::class.java)
        val p1 = Pair(tv_admin_home_site_chat as View, getString(R.string.txt_adminhome_device_change_request))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@AdminActivity, p1)
        startActivity(intent, options.toBundle())
    }

    private fun updateFcm() {
        val deviceToken: String? = FirebaseInstanceId.getInstance().token
        val map = HashMap<String, Any>()
        map[Constants.USER_TOKEN] = deviceToken.toString()
        db.collection(Constants.COLLECTION_USER)
                .document(useremail)
                .set(map, SetOptions.merge())
        if (usertype == "user") {
            FirebaseMessaging.getInstance().subscribeToTopic("notification");
        }
    }

    private fun showExitWarning() {
        val mAlert = AlertDialog.Builder(this).create()
        mAlert.setTitle(getString(R.string.app_name))
        mAlert.setMessage(getString(R.string.exit_message))
        mAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, which ->
            isLoggedIn = false
            displayName = ""
            useremail = ""
            userpassword = ""
            usertype = ""
            mAlert.dismiss()
            finish()
            startActivity(Intent(this@AdminActivity, StartUpActivity::class.java))
        })
        mAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), DialogInterface.OnClickListener { dialog, which ->
            mAlert.dismiss()
            finish()
        })
        mAlert.show()

    }

    override fun onBackPressed() {
        showExitWarning()
    }
}
