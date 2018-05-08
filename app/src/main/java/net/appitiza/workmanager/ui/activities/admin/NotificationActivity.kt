package net.appitiza.workmanager.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.item_toolbar.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.adapter.AdminSpnrUserAdapter
import net.appitiza.workmanager.ui.activities.interfaces.UserClick
import net.appitiza.workmanager.model.UserListdata
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.util.*

class NotificationActivity : BaseActivity(), UserClick {


    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mUserList: ArrayList<UserListdata>
    private lateinit var userAdapter: AdminSpnrUserAdapter
    private var user: UserListdata? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        setActionbar()
        initializeFireBase()
        getUser()
        setClick()
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
        mUserList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setClick() {
        tv_admin_notification_send.setOnClickListener { sendNotification(et_admin_notification_title.text.toString(), et_admin_notification_message.text.toString()) }

        spnr_admin_notificaion_to.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
            }

        }
    }

    private fun getUser() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_site))
        mProgress?.setCancelable(false)
        mProgress?.show()

        db.collection(Constants.COLLECTION_USER)
                .whereEqualTo(Constants.USER_TYPE, "user")
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()
                    if (fetchall_task.isSuccessful) {
                        val data = UserListdata()
                        data.emailId = "all"
                        data.username = "Send To All"
                        mUserList.add(data)
                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data = UserListdata()
                            data.emailId = document.data[Constants.USER_EMAIL].toString()
                            data.username = document.data[Constants.USER_DISPLAY_NAME].toString()
                            mUserList.add(data)

                        }

                        userAdapter = AdminSpnrUserAdapter(this, mUserList, this)
                        spnr_admin_notificaion_to.adapter = userAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }

    override fun onUserClick(data: UserListdata) {

    }

    private fun sendNotification(title: String, message: String) {
        if (validation(title, message)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.sending_notification))
            mProgress?.setCancelable(false)
            mProgress?.show()

            // Sign in success, update UI with the signed-in user's information
            val map = HashMap<String, Any>()
            map[Constants.NOTIFICATION_TITLE] = et_admin_notification_title.text.toString()
            map[Constants.NOTIFICATION_MESSAGE] = et_admin_notification_message.text.toString()
            map[Constants.NOTIFICATION_TO] = user?.emailId.toString()
            map[Constants.NOTIFICATION_TIME] = FieldValue.serverTimestamp()

            db.collection(Constants.COLLECTION_NOTIFICATION)
                    .document()
                    .set(map, SetOptions.merge())
                    .addOnCompleteListener { send_task ->
                        if (send_task.isSuccessful) {
                            mProgress!!.dismiss()
                            Utils.showDialog(this, "Notification Sent")
                            et_admin_notification_title.setText("")
                            et_admin_notification_message.setText("")

                        } else {
                            mProgress!!.hide()
                            Utils.showDialog(this, send_task.exception?.message.toString())
                        }
                    }


        } else {
            Utils.showDialog(this, "Please fill all details")

        }
    }

    private fun validation(title: String, message: String): Boolean {
        return if (TextUtils.isEmpty(title)) {
            showValidationWarning(getString(R.string.title_missing))
            false
        } else if (TextUtils.isEmpty(message)) {
            showValidationWarning(getString(R.string.message_missing))
            false
        } else if (TextUtils.isEmpty(user?.emailId.toString())) {
            showValidationWarning(getString(R.string.user_missing))
            false
        } else {
            true
        }
    }
}
