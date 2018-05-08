package net.appitiza.workmanager.ui.activities.admin

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_admin_sites.*
import kotlinx.android.synthetic.main.item_toolbar.*
import net.appitiza.workmanager.BuildConfig
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.adapter.AdminSiteAdapter
import net.appitiza.workmanager.ui.activities.interfaces.AdminSiteClick
import net.appitiza.workmanager.model.SiteListdata
import net.appitiza.workmanager.utils.Utils


class AdminSitesActivity : BaseActivity(), AdminSiteClick {
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mSiteList: ArrayList<SiteListdata>
    private lateinit var adapter: AdminSiteAdapter
    val LOCATION_SETTINGS = 2
    private val TAG = "LOCATION"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_sites)
        setActionbar()
        initializeFireBase()
        setClick()
        getAll()
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
        rv_admin_site_all.layoutManager = LinearLayoutManager(this)

        mSiteList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

    }

    private fun setClick() {
        fab_admin_add_site.setOnClickListener {

            addSite()


        }
        tv_admin_site_all.setOnClickListener {
            mSiteList.clear()
            getAll()

        }
        tv_admin_site_undergoing.setOnClickListener {
            mSiteList.clear()
            getUndergoing()
        }
        tv_admin_site_completed.setOnClickListener {
            mSiteList.clear()
            getcompleted()
        }
    }

    private fun addSite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!checkPermissions()) {
                requestPermissions()
            } else {
                val intent = Intent(this@AdminSitesActivity, CreateSiteActivity::class.java)
                startActivity(intent)
            }

        } else {
            val intent = Intent(this@AdminSitesActivity, CreateSiteActivity::class.java)
            startActivity(intent)
        }


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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                val intent = Intent(this@AdminSitesActivity, CreateSiteActivity::class.java)
                startActivity(intent)
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

    private fun getAll() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_site))
        mProgress?.setCancelable(false)
        mProgress?.show()
        tv_admin_site_all.setTextColor(ContextCompat.getColor(this, R.color.text_clicked))
        tv_admin_site_completed.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_admin_site_undergoing.setTextColor(ContextCompat.getColor(this, R.color.white))

        db.collection(Constants.COLLECTION_SITE)
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
                            data.lat = document.data[Constants.SITE_LAT].toString().toDouble()
                            data.lon = document.data[Constants.SITE_LON].toString().toDouble()
                            //  data.location = document.data[Constants.SITE_LOCATION]
                            data.status = document.data[Constants.SITE_STATUS].toString()

                            mSiteList.add(data)

                        }
                        adapter = AdminSiteAdapter(mSiteList, this)
                        rv_admin_site_all.adapter = adapter

                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())

                    }
                }


    }

    private fun getUndergoing() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_undergoing_site))
        mProgress?.setCancelable(false)
        mProgress?.show()
        tv_admin_site_all.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_admin_site_completed.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_admin_site_undergoing.setTextColor(ContextCompat.getColor(this, R.color.text_clicked))
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
                            mSiteList.add(data)

                        }
                        adapter = AdminSiteAdapter(mSiteList, this)
                        rv_admin_site_all.adapter = adapter

                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())

                    }
                }
    }

    private fun getcompleted() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_completed_site))
        mProgress?.setCancelable(false)
        mProgress?.show()
        tv_admin_site_all.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_admin_site_completed.setTextColor(ContextCompat.getColor(this, R.color.text_clicked))
        tv_admin_site_undergoing.setTextColor(ContextCompat.getColor(this, R.color.white))
        db.collection(Constants.COLLECTION_SITE)
                .whereEqualTo(Constants.SITE_STATUS, "complete")
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
                            mSiteList.add(data)

                        }
                        adapter = AdminSiteAdapter(mSiteList, this)
                        rv_admin_site_all.adapter = adapter

                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())

                    }
                }
    }

    override fun onSiteClick(data: SiteListdata) {
        val intent = Intent(this@AdminSitesActivity, AdminEditSiteActivity::class.java)
        intent.putExtra("site_data", data)
        startActivity(intent)
    }



}
