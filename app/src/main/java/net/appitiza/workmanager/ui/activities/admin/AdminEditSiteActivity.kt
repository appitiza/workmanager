package net.appitiza.workmanager.ui.activities.admin

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_admin_edit_site.*
import kotlinx.android.synthetic.main.activity_admin_sites.*
import kotlinx.android.synthetic.main.item_toolbar.*
import net.appitiza.workmanager.BuildConfig
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.adapter.SiteStatussAdapter
import net.appitiza.workmanager.adapter.SiteTypesAdapter
import net.appitiza.workmanager.model.SiteListdata
import net.appitiza.workmanager.utils.Utils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class AdminEditSiteActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private var type_list = ArrayList<String>()
    private var status_list = ArrayList<String>()
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mTypeAdapter: SiteTypesAdapter
    private lateinit var mStatusAdapter: SiteStatussAdapter
    private var data: SiteListdata = SiteListdata()


    private val TAG = "AdminEdit"
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    private lateinit var mLocation: Location
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private lateinit var locationManager: LocationManager
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    private val mSelectedCalender = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_site)
        setActionbar()
        initializeFireBase()
        setSiteType()
        setStatusType()
        setClick()
        getData()
    }

    private fun setActionbar() {

        val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        if (getSupportActionBar() != null) {
            tv_title.text = getString(R.string.set_salary)

            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
    private fun initializeFireBase() {
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!checkPermissions()) {
                requestPermissions()
            } else {
                if(checkLocation()) {
                    fetchGPS()
                }
            }

        } else {
            if(checkLocation()) {
                fetchGPS()
            }
        }
    }

    fun getData() {
        data = intent?.getSerializableExtra("site_data") as SiteListdata

        et_admin_edit_site_name.setText(data.sitename.toString())
        if (data.type.equals("Interior")) {
            spnr_admin_edit_type.setSelection(0)
        } else {
            spnr_admin_edit_type.setSelection(1)
        }
        et_admin_edit_start_date.setText(data.date.toString())
        et_admin_edit_project_cost.setText(data.cost.toString())
        et_admin_edit_contact_number.setText(data.contact.toString())
        et_admin_edit_contact_person.setText(data.person.toString())
        if (data.status.equals("undergoing")) {
            spnr_admin_edit_status.setSelection(0)
        } else {
            spnr_admin_edit_status.setSelection(1)
        }
    }

    private fun setSiteType() {

        type_list.add("Interior")
        type_list.add("Exterior")
        mTypeAdapter = SiteTypesAdapter(this, type_list)
        spnr_admin_edit_type.adapter = mTypeAdapter
        //spnr_admin_edit_type.setAdapter(mTypeAdapter)

    }

    private fun setStatusType() {
        status_list.add("undergoing")
        status_list.add("complete")
        mStatusAdapter = SiteStatussAdapter(this, status_list)
        spnr_admin_edit_status.adapter = mStatusAdapter
        //spnr_admin_edit_status.setAdapter(mTypeAdapter)
    }

    private fun setClick() {
        tv_admin_edit_edit.setOnClickListener {
            if (validation()) {
                val map = createMap()
                db.collection(Constants.COLLECTION_SITE)
                        .document(data.siteid.toString())
                        .set(map, SetOptions.merge())
                        .addOnCompleteListener { add_task ->
                            if (add_task.isSuccessful) {
                                mProgress!!.dismiss()
                                Toast.makeText(this@AdminEditSiteActivity, "Edited",
                                        Toast.LENGTH_SHORT).show()
                                finish()

                            } else {
                                mProgress!!.hide()
                                Toast.makeText(this@AdminEditSiteActivity, add_task.exception?.message.toString(),
                                        Toast.LENGTH_SHORT).show()
                            }
                        }


            }

        }
        et_admin_edit_start_date.setOnClickListener { loadCalendar() }
    }

    private fun validation(): Boolean {
        if (TextUtils.isEmpty(et_admin_edit_site_name.text.toString())) {
            showValidationWarning(getString(R.string.site_name_missing))
            return false
        } else if (TextUtils.isEmpty(et_admin_edit_start_date.text.toString())) {
            showValidationWarning(getString(R.string.site_date_missing))
            return false
        } else if (TextUtils.isEmpty(et_admin_edit_project_cost.text.toString())) {
            showValidationWarning(getString(R.string.site_cost_missing))
            return false
        } else if (TextUtils.isEmpty(et_admin_edit_contact_number.text.toString())) {
            showValidationWarning(getString(R.string.site_contact_number_missing))
            return false
        } else if (TextUtils.isEmpty(et_admin_edit_contact_person.text.toString())) {
            showValidationWarning(getString(R.string.site_name_missing))
            return false
        } else {
            return true
        }
    }

    private fun createMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map[Constants.SITE_NAME] = et_admin_edit_site_name.text.toString()
        map[Constants.SITE_TYPE] = spnr_admin_edit_type.selectedItem.toString()
        map[Constants.SITE_DATE] = et_admin_edit_start_date.text.toString()
        map[Constants.SITE_COST] = et_admin_edit_project_cost.text.toString()
        map[Constants.SITE_CONTACT] = et_admin_edit_contact_number.text.toString()
        map[Constants.SITE_PERSON] = et_admin_edit_contact_person.text.toString()
        map[Constants.SITE_LAT] = mLocation!!.latitude
        map[Constants.SITE_LON] = mLocation!!.longitude
        map[Constants.SITE_LOCATION] = GeoPoint(mLocation!!.latitude,mLocation!!.longitude)
        map[Constants.SITE_STATUS] = spnr_admin_edit_status.selectedItem.toString()
        return map
    }

    private fun loadCalendar() {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = android.app.DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    mSelectedCalender.set(year, monthOfYear, dayOfMonth)
                    et_admin_edit_start_date.setText(Utils.convertDate(mSelectedCalender.timeInMillis, "dd MMM yyyy"))
                }, mYear, mMonth, mDay)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000


        datePickerDialog.setTitle(null)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }



    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
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

    override fun onStart() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect()
        }
        super.onStart()
    }

    override fun onStop() {
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect();
        }
        super.onStop()
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
                if(checkLocation()) {
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

        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.errorCode)
    }

    override fun onLocationChanged(location: Location) {


        val msg = "Updated Location: Latitude " + location.latitude.toString() + location.longitude
        mLocation = location
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


    }

    override fun onConnected(p0: Bundle?) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        startLocationUpdates();

        val fusedLocationProviderClient:
                FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mLocation = location;

                    }
                })
    }


}
