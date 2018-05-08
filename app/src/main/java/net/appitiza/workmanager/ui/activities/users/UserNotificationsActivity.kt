package net.appitiza.workmanager.ui.activities.users

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_user_notifications.*
import kotlinx.android.synthetic.main.item_toolbar.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.adapter.UserNotificationAdapter
import net.appitiza.workmanager.ui.activities.interfaces.NotificationClick
import net.appitiza.workmanager.model.NotificationData
import net.appitiza.workmanager.utils.PreferenceHelper
import java.util.*


class UserNotificationsActivity : BaseActivity(), NotificationClick {


    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mNotificationList: ArrayList<NotificationData>
    private lateinit var adapter: UserNotificationAdapter
    private lateinit var allNotification: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_notifications)
        setActionbar()
        initializeFireBase()
        getAllNotification()
        getMyNotification()
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
        rv_notification_list.layoutManager = LinearLayoutManager(this)
        mNotificationList = arrayListOf()
        adapter = UserNotificationAdapter(mNotificationList, this)
        rv_notification_list.adapter = adapter
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun getAllNotification() {
        mNotificationList.clear()
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_undergoing_site))
        mProgress?.setCancelable(false)
        mProgress?.show()
       /* db.collection(Constants.COLLECTION_NOTIFICATION)
                .whereEqualTo(Constants.NOTIFICATION_TO, "all")
                .orderBy(Constants.NOTIFICATION_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data = NotificationData()
                            data.notificationId = document.id
                            data.title = document.data[Constants.NOTIFICATION_TITLE].toString()
                            data.message = document.data[Constants.NOTIFICATION_MESSAGE].toString()
                            data.time = document.data[Constants.NOTIFICATION_TIME].toString()
                            data.to = document.data[Constants.NOTIFICATION_TO].toString()
                            mNotificationList.add(data)

                        }
                        adapter.notifyDataSetChanged()

                    } else {
                        Toast.makeText(this@UserNotificationsActivity, fetchall_task.exception.toString(),
                                Toast.LENGTH_SHORT).show()
                        Log.e("link", fetchall_task.exception.toString())

                    }
                }*/
        db.collection(Constants.COLLECTION_NOTIFICATION)
                .whereEqualTo(Constants.NOTIFICATION_TO, "all")
                .orderBy(Constants.NOTIFICATION_TIME, Query.Direction.DESCENDING)
                .addSnapshotListener(this,
                        { querySnapshot: QuerySnapshot, e: FirebaseFirestoreException? ->

                            mProgress?.dismiss()
                            for (document in querySnapshot!!.documents) {
                                val data = NotificationData()
                                data.notificationId = document.id
                                data.title = document.data[Constants.NOTIFICATION_TITLE].toString()
                                data.message = document.data[Constants.NOTIFICATION_MESSAGE].toString()
                                data.time = document.data[Constants.NOTIFICATION_TIME].toString()
                                data.to = document.data[Constants.NOTIFICATION_TO].toString()
                                mNotificationList.add(data)

                            }
                            adapter.notifyDataSetChanged()
                        })
    }

    private fun getMyNotification() {
       /* mNotificationList.clear()
        db.collection(Constants.COLLECTION_NOTIFICATION)
                .whereEqualTo(Constants.NOTIFICATION_TO, useremail)
                .orderBy(Constants.NOTIFICATION_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { fetchall_task ->

                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data = NotificationData()
                            data.notificationId = document.id
                            data.title = document.data[Constants.NOTIFICATION_TITLE].toString()
                            data.message = document.data[Constants.NOTIFICATION_MESSAGE].toString()
                            data.time = document.data[Constants.NOTIFICATION_TIME].toString()
                            data.to = document.data[Constants.NOTIFICATION_TO].toString()
                            mNotificationList.add(data)

                        }
                        adapter.notifyDataSetChanged()

                    } else {
                        Toast.makeText(this@UserNotificationsActivity, fetchall_task.exception.toString(),
                                Toast.LENGTH_SHORT).show()

                    }
                }*/
        db.collection(Constants.COLLECTION_NOTIFICATION)
                .whereEqualTo(Constants.NOTIFICATION_TO, useremail)
                .orderBy(Constants.NOTIFICATION_TIME, Query.Direction.DESCENDING)
                .addSnapshotListener(this,
                        { querySnapshot: QuerySnapshot, e: FirebaseFirestoreException? ->

                            mProgress?.dismiss()
                            for (document in querySnapshot!!.documents) {
                                val data = NotificationData()
                                data.notificationId = document.id
                                data.title = document.data[Constants.NOTIFICATION_TITLE].toString()
                                data.message = document.data[Constants.NOTIFICATION_MESSAGE].toString()
                                data.time = document.data[Constants.NOTIFICATION_TIME].toString()
                                data.to = document.data[Constants.NOTIFICATION_TO].toString()
                                mNotificationList.add(data)

                            }
                            adapter.notifyDataSetChanged()
                        })


    }

    override fun onClick(data: NotificationData) {
    }
}
