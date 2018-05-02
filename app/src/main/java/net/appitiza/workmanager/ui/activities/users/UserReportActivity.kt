package net.appitiza.workmanager.ui.activities.users

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_admin_sites.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_user_report.*
import net.appitiza.workmanager.BuildConfig
import net.appitiza.workmanager.R
import net.appitiza.workmanager.R.id.tv_user_report_checkin
import net.appitiza.workmanager.R.id.tv_user_report_checkout
import net.appitiza.workmanager.adapter.UserCheckSiteAdapter
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.model.CurrentCheckIndata
import net.appitiza.workmanager.model.SiteListdata
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.ui.activities.admin.AdminEditSiteActivity
import net.appitiza.workmanager.ui.activities.interfaces.UserSiteClick
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.text.SimpleDateFormat
import java.util.*


class UserReportActivity : BaseActivity(), UserSiteClick, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var userimei by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_IMEI, "")
    private var salary by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_SALARY, 0)

    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mSiteList: ArrayList<SiteListdata>
    private lateinit var ciadapter: UserCheckSiteAdapter
    private lateinit var coadapter: UserCheckSiteAdapter
    private val mCheckInData: CurrentCheckIndata = CurrentCheckIndata()
    private var checkinSite: SiteListdata = SiteListdata()
    private var checkoutSite: SiteListdata = SiteListdata()


    private val TAG = "UserReport"
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    private lateinit var mLocation: Location
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private lateinit var locationManager: LocationManager
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_IMEI_PERMISSIONS_REQUEST_CODE = 33
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_report)
        initializeFireBase()
        getSites()
        getCheckInInfo()
        setClick()
    }

    private fun initializeFireBase() {

        mSiteList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!checkPermissions()) {
                requestPermissions()
            } else {
                if (checkLocation()) {
                    fetchGPS()
                }
            }

        } else {
            if (checkLocation()) {
                fetchGPS()
            }
        }
    }

    private fun checkPermissions(): Boolean {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else
        {
            return true
        }
    }
    private fun checkIMEIPermissions(): Boolean {


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE)
        }
        else
        {
            return true
        }
    }
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    fab_admin_add_site,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, View.OnClickListener {
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    })
                    .show()
        } else {
            Log.i(TAG, "Requesting permission")

            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)

        }
    }
    private fun requestIMEIPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)

        if (shouldProvideRationale) {
            Snackbar.make(
                    fab_admin_add_site,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, View.OnClickListener {
                        requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE),
                                REQUEST_IMEI_PERMISSIONS_REQUEST_CODE)
                    })
                    .show()
        } else {

            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQUEST_IMEI_PERMISSIONS_REQUEST_CODE)

        }
    }
    override fun onStart() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect()
        }
        super.onStart()
    }

    override fun onStop() {
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
        super.onStop()
    }

    private fun setClick() {
        tv_user_report_checkin.setOnClickListener {
            if (TextUtils.isEmpty(mCheckInData.siteid)) {
                if (Utils.isWithinRange(checkinSite.lat, checkinSite.lon, mLocation.latitude, mLocation.longitude, 1f)) {
                    if (checkIMEIPermissions()) {
                        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        val device_id = tm.deviceId
                        if(device_id == userimei) {
                            insertHistory()
                        }
                        else{
                            Utils.showDialog(this, getString(R.string.device_donot_match))
                        }
                    } else {
                        requestIMEIPermissions()
                    }


                } else {


                    Utils.showDialog(this, getString(R.string.not_in_range))

                }
            } else {
                mProgress!!.hide()
                Utils.showDialog(this, "already checked in " + mCheckInData.checkintime + "  \nPlease check out")
            }

        }
        tv_user_report_checkout.setOnClickListener {

            if (!TextUtils.isEmpty(mCheckInData.siteid)) {
                if (mCheckInData.siteid.equals(checkoutSite.siteid)) {
                    if (!TextUtils.isEmpty(et_users_site_payment.text.toString())) {

                        if (mLocation != null && mLocation.latitude != 0.0 && mLocation.longitude != 0.0) {
                            if (Utils.isWithinRange(checkoutSite.lat, checkoutSite.lon, mLocation.latitude, mLocation.longitude, 10f)) {

                                val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                                val device_id = tm.deviceId
                                if(device_id == userimei) {
                                    updateHistory()
                                }
                                else{
                                    Utils.showDialog(this, getString(R.string.device_donot_match))
                                }
                            } else {
                                Utils.showDialog(this, "Not in Range")

                            }
                        }
                    } else {
                        Utils.showDialog(this, getString(R.string.please_provide_payment_amount))
                    }
                } else {
                    Utils.showDialog(this, "You have checked in at " + mCheckInData.sitename + " \nPlease check out from same site")
                }

            } else {

                Utils.showDialog(this, "You need to check in first")
            }

        }
        spnr_users_check_out_site.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                checkoutSite = mSiteList[position]
            }

        }
        spnr_users_check_in_site.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                checkinSite = mSiteList[position]
            }

        }
    }

    private fun checkInData() {

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.checking_in_user))
        mProgress?.setCancelable(false)
        mProgress?.show()
        val map = HashMap<String, Any>()
        map[Constants.CHECKIN_SITE] = checkinSite.siteid.toString()
        map[Constants.CHECKIN_SITENAME] = checkinSite.sitename.toString()
        map[Constants.CHECKIN_CHECKIN] = FieldValue.serverTimestamp()
        map[Constants.CHECKIN_USEREMAIL] = useremail


        db.collection(Constants.COLLECTION_CHECKIN_DATA)
                .document(mCheckInData.documentid.toString())
                .set(map, SetOptions.merge())
                .addOnCompleteListener { checkin_task ->
                    if (checkin_task.isSuccessful) {
                        mProgress!!.dismiss()
                        Utils.showDialog(this, "CHECKED IN")
                        finish()

                    } else {
                        mProgress!!.hide()
                        Utils.showDialog(this, checkin_task.exception?.message.toString())
                    }
                }


    }

    private fun checkinClear() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.checking_out_user))
        mProgress?.setCancelable(false)
        mProgress?.show()
        db.collection(Constants.COLLECTION_CHECKIN_DATA)
                .document(mCheckInData.documentid.toString())
                .delete()
                .addOnCompleteListener { clear_task ->
                    if (clear_task.isSuccessful) {
                        mProgress!!.dismiss()

                        Utils.showDialog(this, "CHECKED OUT")
                        finish()

                    } else {
                        mProgress!!.hide()
                        Utils.showDialog(this, clear_task.exception?.message.toString())
                    }
                }
    }

    private fun insertHistory() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.syn))
        mProgress?.setCancelable(false)
        mProgress?.show()
        val map = HashMap<String, Any>()
        map[Constants.CHECKIN_SITE] = checkinSite.siteid.toString()
        map[Constants.CHECKIN_SITENAME] = checkinSite.sitename.toString()
        map[Constants.CHECKIN_CHECKIN] = FieldValue.serverTimestamp()
        map[Constants.CHECKIN_USEREMAIL] = useremail
        map[Constants.CHECKIN_USERNAME] = displayName


        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .add(map)
                .addOnSuccessListener { documentReference ->
                    mCheckInData.documentid = documentReference.id
                    mCheckInData.siteid = checkinSite.siteid.toString()
                    mCheckInData.sitename = checkinSite.sitename.toString()
                    mCheckInData.useremail = useremail
                    mProgress!!.dismiss()
                    checkInData()

                }
                .addOnFailureListener { e ->
                    mProgress!!.dismiss()

                }
    }

    private fun updateHistory() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.syn))
        mProgress?.setCancelable(false)
        mProgress?.show()
        val map = HashMap<String, Any>()
        map[Constants.CHECKIN_CHECKOUT] = FieldValue.serverTimestamp()
        map[Constants.CHECKIN_PAYMENT] = et_users_site_payment.text.toString()
        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .document(mCheckInData.documentid.toString())
                .set(map, SetOptions.merge())
                .addOnCompleteListener { sync_task ->
                    if (sync_task.isSuccessful) {
                        mProgress!!.dismiss()

                        checkinClear()

                    } else {
                        mProgress!!.hide()

                    }
                }
    }

    private fun getCheckInInfo() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.get_checkin_info))
        mProgress?.setCancelable(false)
        mProgress?.show()
        db.collection(Constants.COLLECTION_CHECKIN_DATA)
                .whereEqualTo(Constants.CHECKIN_USEREMAIL, useremail)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()
                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {

                            mCheckInData.documentid = document.id
                            mCheckInData.siteid = document.data[Constants.CHECKIN_SITE].toString()
                            mCheckInData.sitename = document.data[Constants.CHECKIN_SITENAME].toString()
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKIN].toString()) && document.data[Constants.CHECKIN_CHECKIN].toString() != "null") {
                                mCheckInData.checkintime = getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time.toLong()
                            }
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKOUT].toString()) && document.data[Constants.CHECKIN_CHECKOUT].toString() != "null") {
                                mCheckInData.checkouttime = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time.toLong()
                            }
                            mCheckInData.useremail = document.data[Constants.CHECKIN_USEREMAIL].toString()

                        }

                        if (!TextUtils.isEmpty(mCheckInData.checkintime.toString()) && mCheckInData.checkintime != 0L && mCheckInData.checkintime.toString() != "null") {
                            tv_user_report_date.text = Utils.convertDate(mCheckInData.checkintime!!.toLong(), "dd MMM yyyy")
                            var total_hours: Double = 0.0
                            val mCalender = Calendar.getInstance()

                            tv_user_report_completed_time.text = Utils.convertHours((mCalender.timeInMillis - mCheckInData.checkintime!!.toLong()))


                        } else {
                            val mCalender = Calendar.getInstance()
                            tv_user_report_date.text = Utils.convertDate(mCalender.timeInMillis, "dd MMM yyyy")
                            tv_user_report_completed_time.text = getString(R.string.not_checked_in_any_where)
                        }
                        if (!TextUtils.isEmpty(mCheckInData.sitename) && !mCheckInData.sitename.equals("null")) {
                            tv_user_report_checkin_at.text = mCheckInData.sitename.toString()
                        } else {
                            tv_user_report_checkin_at.text = getString(R.string.not_checked_in_any_where)
                        }

                    }
                }


    }

    private fun getSites() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_site))
        mProgress?.setCancelable(false)
        mProgress?.show()

        db.collection(Constants.COLLECTION_SITE)
                .whereEqualTo(Constants.SITE_STATUS, "undergoing")
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()
                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data: SiteListdata = SiteListdata()
                            data.siteid = document.id
                            data.sitename = document.data[Constants.SITE_NAME].toString()
                            data.type = document.data[Constants.SITE_TYPE].toString()
                            data.date = document.data[Constants.SITE_DATE].toString()
                            data.cost = document.data[Constants.SITE_COST].toString()
                            data.contact = document.data[Constants.SITE_CONTACT].toString()
                            data.person = document.data[Constants.SITE_PERSON].toString()
                            data.status = document.data[Constants.SITE_STATUS].toString()
                            data.lat = document.data[Constants.SITE_LAT].toString().toDouble()
                            data.lon = document.data[Constants.SITE_LON].toString().toDouble()
                            //  data.location = document.data[Constants.SITE_LOCATION]
                            mSiteList.add(data)

                        }
                        ciadapter = UserCheckSiteAdapter(this, mSiteList, this)
                        coadapter = UserCheckSiteAdapter(this, mSiteList, this)
                        spnr_users_check_in_site.adapter = ciadapter
                        spnr_users_check_out_site.adapter = coadapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }

    private fun getDate(date: String): Date {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val value: Date = format.parse(date)
        return value
    }

    override fun onSiteClick(data: SiteListdata) {
        val intent = Intent(this@UserReportActivity, AdminEditSiteActivity::class.java)
        intent.putExtra("site_data", data)
        startActivity(intent)
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private fun fetchGPS() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
        dialog.show()
    }

    protected fun startLocationUpdates() {

        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                if (checkLocation()) {
                    fetchGPS()
                }
            } else {
                // Permission denied.
                Snackbar.make(fab_admin_add_site,
                        R.string.permission_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, View.OnClickListener {
                            val intent: Intent = Intent()
                            intent.action =  Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri: Uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        })
                        .show()

            }
        }
        else if (requestCode == REQUEST_IMEI_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                if (checkLocation()) {
                    fetchGPS()
                }
            } else {
                // Permission denied.
                Snackbar.make(fab_admin_add_site,
                        R.string.permission_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, View.OnClickListener {
                            val intent: Intent = Intent()
                            intent.action =  Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri: Uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        })
                        .show()

            }
        }


    }

    override fun onConnectionSuspended(p0: Int) {

        Log.i(TAG, "Connection Suspended")
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.errorCode)
    }

    override fun onLocationChanged(location: Location) {


        var msg = "Updated Location: Latitude " + location.latitude.toString() + location.longitude
        mLocation = location


    }

    override fun onConnected(p0: Bundle?) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        startLocationUpdates()

        val fusedLocationProviderClient:
                FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mLocation = location

                    }
                })
    }
}
