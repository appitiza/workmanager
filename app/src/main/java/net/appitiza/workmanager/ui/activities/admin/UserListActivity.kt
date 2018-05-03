package net.appitiza.workmanager.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_usel_list.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.adapter.ChatUserAdapter
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.model.UserListdata
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.ui.activities.interfaces.ChatUserClick
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.util.*

class UserListActivity : BaseActivity(), ChatUserClick {
    override fun onClick(data: UserListdata) {

    }

    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mUserList: ArrayList<UserListdata>
    private lateinit var userAdapter: ChatUserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usel_list)
        initializeFireBase()
        getUser()
    }

    private fun initializeFireBase() {


        rv_user_list.layoutManager = LinearLayoutManager(this)
        mUserList = arrayListOf()
        userAdapter = ChatUserAdapter(mUserList, this,this)
        rv_user_list.adapter = userAdapter
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }


    private fun getUser() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_site))
        mProgress?.setCancelable(false)
        mProgress?.show()

        db.collection(Constants.COLLECTION_USER)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()
                    if (fetchall_task.isSuccessful) {

                        for (document in fetchall_task.result) {
                            val data = UserListdata()
                            data.emailId = document.data[Constants.USER_EMAIL].toString()
                            data.username = document.data[Constants.USER_DISPLAY_NAME].toString()
                            data.status = document.data[Constants.USER_STATUS].toString()
                            data.token = document.data[Constants.USER_TOKEN].toString()
                            data.imei = document.data[Constants.USER_IMEI].toString()
                          //  data.salary = document.data[Constants.USER_SALARY].toLo
                            data.type = document.data[Constants.USER_TYPE].toString()
                            data.image = document.data[Constants.USER_IMAGE].toString()
                            data.thumb = document.data[Constants.USER_THUMB].toString()
                           // data.seen = document.data[Constants.USER_LASTSEEN].toString()
                           // data.time = document.data[Constants.USER_REG_TIME].toString()




                            mUserList.add(data)

                        }

                        userAdapter.notifyDataSetChanged()
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }
}
