package net.appitiza.workmanager.ui.activities.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_device_change_requests.*
import kotlinx.android.synthetic.main.item_toolbar.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.adapter.AdminChangeDeviceRequestAdapter
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.model.ChangeDeviceRequestData
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.ui.activities.interfaces.ChangeRequestClick
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.util.*


class DeviceChangeRequestsActivity : BaseActivity(), ChangeRequestClick {


    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mRequestList: ArrayList<ChangeDeviceRequestData>
    private lateinit var adapter: AdminChangeDeviceRequestAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_change_requests)
        setActionbar()
        initializeFireBase()
        getAllRequest()
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
        rv_device_change_list.layoutManager = LinearLayoutManager(this)
        mRequestList = arrayListOf()
        adapter = AdminChangeDeviceRequestAdapter(this, mRequestList, this)
        rv_device_change_list.adapter = adapter
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun getAllRequest() {
        mRequestList.clear()
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_undergoing_site))
        mProgress?.setCancelable(false)
        mProgress?.show()
        db.collection(Constants.COLLECTION_DEVICE_CHANGE_REQUEST)
                .orderBy(Constants.DEVICE_CHANGE_REQUEST_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            val data = ChangeDeviceRequestData()
                            data.requestId = document.id
                            data.imei = document.data[Constants.DEVICE_CHANGE_REQUEST_IMEI].toString()
                            data.current_imei = document.data[Constants.DEVICE_CHANGE_REQUEST_CURRENT_IMEI].toString()
                            data.reason = document.data[Constants.DEVICE_CHANGE_REQUEST_REASON].toString()
                            data.time = document.data[Constants.DEVICE_CHANGE_REQUEST_TIME].toString()
                            data.status = document.data[Constants.DEVICE_CHANGE_REQUEST_STATUS].toString()
                            data.useremail = document.data[Constants.DEVICE_CHANGE_REQUEST_EMAIL].toString()
                            data.displayName = document.data[Constants.DEVICE_CHANGE_REQUEST_NAME].toString()
                            mRequestList.add(data)

                        }
                        adapter.notifyDataSetChanged()

                    }
                }

    }

    override fun onClick(data: ChangeDeviceRequestData) {

        val mdialog: AlertDialog = AlertDialog.Builder(this).create()
        mdialog.setTitle(getString(R.string.app_name))
        mdialog.setMessage(getString(R.string.would_you_like_to_change, data.current_imei, data.imei, data.displayName))
        mdialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok), DialogInterface.OnClickListener { dialog, which ->
            reset(data.imei.toString(), data.useremail.toString(), data.requestId.toString())
            mdialog.dismiss()

        })
        mdialog.show()
    }

    private fun reset(imei: String, email: String, id: String) {
        if (validation(imei, email)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.sending_notification))
            mProgress?.setCancelable(false)
            mProgress?.show()

            val map = HashMap<String, Any>()
            map[Constants.USER_IMEI] = imei
            db.collection(Constants.COLLECTION_USER)
                    .document(email)
                    .set(map, SetOptions.merge())
                    .addOnCompleteListener { send_task ->
                        if (send_task.isSuccessful) {
                            mProgress!!.dismiss()
                            Utils.showDialog(this, getString(R.string.device_reset_done))

                            val map = HashMap<String, Any>()
                            map[Constants.DEVICE_CHANGE_REQUEST_STATUS] = "Done"
                            db.collection(Constants.COLLECTION_DEVICE_CHANGE_REQUEST)
                                    .document(id)
                                    .set(map, SetOptions.merge())
                                    .addOnCompleteListener { send_task ->
                                        if (send_task.isSuccessful) {

                                            getAllRequest()
                                        }
                                    }

                        } else {
                            mProgress!!.hide()
                            Utils.showDialog(this, send_task.exception?.message.toString())
                        }
                    }


        } else {
            Utils.showDialog(this, "Please fill all details")

        }
    }

    private fun validation(imei: String, email: String): Boolean {
        return if (TextUtils.isEmpty(imei)) {
            showValidationWarning(getString(R.string.title_missing))
            false
        } else if (TextUtils.isEmpty(email)) {
            showValidationWarning(getString(R.string.user_missing))
            false
        } else {
            true
        }
    }
}
