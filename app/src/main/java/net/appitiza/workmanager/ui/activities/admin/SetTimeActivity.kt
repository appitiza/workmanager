package net.appitiza.workmanager.ui.activities.admin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_set_time.*
import kotlinx.android.synthetic.main.activity_user_report.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.adapter.AdminSpnrUserAdapter
import net.appitiza.workmanager.adapter.AdminSprSiteAdapter
import net.appitiza.workmanager.adapter.UserCheckSiteAdapter
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.model.CurrentCheckIndata
import net.appitiza.workmanager.model.SiteListdata
import net.appitiza.workmanager.model.UserListdata
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.ui.activities.interfaces.UserClick
import net.appitiza.workmanager.ui.activities.interfaces.UserSiteClick
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.util.*

class SetTimeActivity : BaseActivity(), UserClick, UserSiteClick {


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

    private lateinit var mSiteList: ArrayList<SiteListdata>
    private lateinit var ciadapter: AdminSprSiteAdapter
    private lateinit var coadapter: AdminSprSiteAdapter
    private val mCheckInData: CurrentCheckIndata = CurrentCheckIndata()
    private var checkinSite: SiteListdata = SiteListdata()
    private var checkoutSite: SiteListdata = SiteListdata()

    private var mCheckinCalendar: Calendar = Calendar.getInstance()
    private var mCheckoutCalendar: Calendar = Calendar.getInstance()
    private var isT1Set: Boolean = false
    private var isT2Set: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_time)
        initializeFireBase()
        getUser()
        getSites()
        setClick()
    }

    private fun initializeFireBase() {
        mUserList = arrayListOf()
        mSiteList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setClick() {
        tv_admin_submit_time.setOnClickListener {
            if (TextUtils.isEmpty(mCheckInData.siteid)) {
                if (validate()) {
                    insertHistory()
                }
            } else {
                mProgress!!.hide()
                Utils.showDialog(this, "already checked in " + mCheckInData.checkintime + "  \nPlease check out")
            }

        }
        et_admin_site_checkin_time.setOnClickListener { loadCalendar(0) }
        et_admin_site_checkout_time.setOnClickListener { loadCalendar(1) }
        spnr_admin_time_user.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
            }

        }
        spnr_admin_check_out_site.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                checkoutSite = mSiteList[position]
            }

        }
        spnr_admin_check_in_site.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                checkinSite = mSiteList[position]
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

                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data = UserListdata()
                            data.emailId = document.data[Constants.USER_EMAIL].toString()
                            data.username = document.data[Constants.USER_DISPLAY_NAME].toString()
                            mUserList.add(data)

                        }

                        userAdapter = AdminSpnrUserAdapter(this, mUserList, this)
                        spnr_admin_time_user.adapter = userAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
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
                        ciadapter = AdminSprSiteAdapter(this, mSiteList, this)
                        coadapter = AdminSprSiteAdapter(this, mSiteList, this)
                        spnr_admin_check_in_site.adapter = ciadapter
                        spnr_admin_check_out_site.adapter = coadapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }

    override fun onUserClick(data: UserListdata) {

    }

    override fun onSiteClick(data: SiteListdata) {

    }

    private fun insertHistory() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.syn))
        mProgress?.setCancelable(false)
        mProgress?.show()
        val map = HashMap<String, Any>()
        map[Constants.CHECKIN_SITE] = checkinSite.siteid.toString()
        map[Constants.CHECKIN_SITENAME] = checkinSite.sitename.toString()
        map[Constants.CHECKIN_CHECKIN] = mCheckinCalendar.time
        map[Constants.CHECKIN_USEREMAIL] = user?.emailId.toString()
        map[Constants.CHECKIN_CHECKOUT] = mCheckoutCalendar.time
        map[Constants.CHECKIN_PAYMENT] = et_admin_site_payment.text.toString()
        map[Constants.CHECKIN_USERNAME] = user?.username.toString()

        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .add(map)
                .addOnSuccessListener { documentReference ->

                    mProgress!!.dismiss()
                    Utils.showDialog(this, getString(R.string.checkin_details_added))

                }
                .addOnFailureListener { e ->
                    mProgress!!.dismiss()

                }
    }

    private fun loadCalendar(from: Int) {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = android.app.DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                    loadTimer(from, year, monthOfYear, dayOfMonth)

                }, mYear, mMonth, mDay)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000


        datePickerDialog.setTitle(null)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }


    private fun loadTimer(from: Int, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)


        val timePickerDialog = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    if (from == 0) {
                        mCheckinCalendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, 1)
                        et_admin_site_checkin_time.setText(Utils.convertDate(mCheckinCalendar.timeInMillis, "dd MMM yyyy HH:mm"))
                        isT1Set = true
                    } else {
                        mCheckoutCalendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, 1)
                        et_admin_site_checkout_time.setText(Utils.convertDate(mCheckoutCalendar.timeInMillis, "dd MMM yyyy HH:mm"))
                        isT2Set = true
                    }

                }, mHour, mMinute, false)
        timePickerDialog.setCancelable(false)
        timePickerDialog.show()
    }

    private fun validate(): Boolean {
        if (TextUtils.isEmpty(et_admin_site_checkin_time.text.toString())) {
            Utils.showDialog(this, getString(R.string.please_provide_checkin_time))
            return false
        } else if (TextUtils.isEmpty(et_admin_site_checkout_time.text.toString())) {
            Utils.showDialog(this, getString(R.string.please_provide_checkout_time))
            return false
        } else if (TextUtils.isEmpty(et_admin_site_payment.text.toString())) {
            Utils.showDialog(this, getString(R.string.please_provide_payment_amount))
            return false
        } else if (mCheckinCalendar.timeInMillis >= mCheckoutCalendar.timeInMillis) {
            Utils.showDialog(this, getString(R.string.checkout_time_should_be_greater_than_checkin))
            return false
        } else if (checkinSite.siteid != checkoutSite.siteid) {
            Utils.showDialog(this, getString(R.string.check_in_not_equal_check_out))
            return false
        }
        else {
            return true
        }

    }
}
