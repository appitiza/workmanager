package net.appitiza.workmanager.ui.activities.admin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_edit_work_report.*
import kotlinx.android.synthetic.main.activity_set_time.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.adapter.AdminSpnrUserAdapter
import net.appitiza.workmanager.adapter.AdminSprSiteAdapter
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.model.CurrentCheckIndata
import net.appitiza.workmanager.model.SiteListdata
import net.appitiza.workmanager.model.UserListdata
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.util.*

class EditWorkReportActivity : BaseActivity() {

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

    private var data: CurrentCheckIndata = CurrentCheckIndata()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_work_report)
        initializeFireBase()
        getUser()
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
        tv_admin_edit_submit_time.setOnClickListener {
            if (TextUtils.isEmpty(mCheckInData.siteid)) {
                if (validate()) {
                    insertHistory()
                }
            } else {
                mProgress!!.hide()
                Utils.showDialog(this, "already checked in " + mCheckInData.checkintime + "  \nPlease check out")
            }

        }
        et_admin_edit_site_checkin_time.setOnClickListener { loadCalendar(0) }
        et_admin_edit_site_checkout_time.setOnClickListener { loadCalendar(1) }

    }

    private fun getUser() {
        data = intent!!.getSerializableExtra("data") as CurrentCheckIndata
        tv_admin_edit_user_displayname.text = data.username
        spnr_admin_edit_check_in_site.text = data.sitename
        spnr_admin_edit_check_out_site.text = data.sitename
        if (data.checkintime != 0L) {
            et_admin_edit_site_checkin_time.setText(Utils.convertDate(data.checkintime, "dd MMM yyyy HH:mm:ss"))

        }
        if (data.checkouttime != 0L) {
            et_admin_edit_site_checkout_time.setText(Utils.convertDate(data.checkouttime, "dd MMM yyyy HH:mm:ss"))
        }
        if (data.checkintime != 0L && data.checkouttime != 0L) {
            et_admin_edit_site_payment.setText(data.payment)
            et_admin_edit_site_payment.setSelection(data.payment!!.length)
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
        map[Constants.CHECKIN_CHECKIN] = mCheckinCalendar.time
        map[Constants.CHECKIN_USEREMAIL] = user?.emailId.toString()
        map[Constants.CHECKIN_CHECKOUT] = mCheckoutCalendar.time
        map[Constants.CHECKIN_PAYMENT] = et_admin_site_payment.text.toString()


        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .document(data.documentid)
                .set(map, SetOptions.merge())
                .addOnSuccessListener { documentReference ->

                    mProgress!!.dismiss()
                    Utils.showDialog(this, getString(R.string.checkin_details_edited))

                }
                .addOnFailureListener { e ->
                    mProgress!!.dismiss()

                }
    }

    private fun loadCalendar(from: Int) {

        val c = Calendar.getInstance()

        if (from == 0) {
            c.timeInMillis = data.checkintime
        } else {
            c.timeInMillis = data.checkouttime
        }
        val datePickerDialog = android.app.DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                    loadTimer(from, year, monthOfYear, dayOfMonth)

                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000


        datePickerDialog.setTitle(null)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }


    private fun loadTimer(from: Int, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        if (from == 0) {
            c.timeInMillis = data.checkintime
        } else {
            c.timeInMillis = data.checkouttime
        }


        val timePickerDialog = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    if (from == 0) {
                        mCheckinCalendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, 1)
                        et_admin_edit_site_checkin_time.setText(Utils.convertDate(mCheckinCalendar.timeInMillis, "dd MMM yyyy HH:mm"))
                        isT1Set = true
                    } else {
                        mCheckoutCalendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, 1)
                        et_admin_edit_site_checkout_time.setText(Utils.convertDate(mCheckoutCalendar.timeInMillis, "dd MMM yyyy HH:mm"))
                        isT2Set = true
                    }

                }, c.get(Calendar.HOUR_OF_DAY),  c.get(Calendar.MINUTE), false)
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
        } else {
            return true
        }

    }
}