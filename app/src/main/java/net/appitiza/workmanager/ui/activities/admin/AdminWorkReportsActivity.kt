package net.appitiza.workmanager.ui.activities.admin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_admin_work_reports.*
import kotlinx.android.synthetic.main.admin_daily_layout.*
import kotlinx.android.synthetic.main.admin_monthly_layout.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.adapter.AdminHistoryAdapter
import net.appitiza.workmanager.adapter.AdminSpnrUserAdapter
import net.appitiza.workmanager.ui.activities.interfaces.AdminWorkHistoryClick
import net.appitiza.workmanager.ui.activities.interfaces.UserClick
import net.appitiza.workmanager.model.CurrentCheckIndata
import net.appitiza.workmanager.model.UserListdata
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class AdminWorkReportsActivity : BaseActivity(), UserClick,AdminWorkHistoryClick {
    override fun onClick(data: CurrentCheckIndata) {
       loadEdit(data)
    }

    override fun onUserClick(data: UserListdata) {

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
    private lateinit var userAdapter: AdminSpnrUserAdapter
    private var user: UserListdata? = null


    private lateinit var mHistoryDisplay: ArrayList<CurrentCheckIndata>
    private lateinit var mHistoryDaily: ArrayList<CurrentCheckIndata>
    private lateinit var mHistoryMonthly: ArrayList<CurrentCheckIndata>
    private lateinit var adapterMonthly: AdminHistoryAdapter

    private val mSelectedCalender = Calendar.getInstance()

    var isDailyClicked = true
    companion object {
        var userSalary: Int = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_work_reports)
        initializeFireBase()
        getUser()
        setClick()
    }

    private fun initializeFireBase() {
        mUserList = arrayListOf()
        rv_adminhistory_list.layoutManager = LinearLayoutManager(this)
        mHistoryDisplay = arrayListOf()
        mHistoryDaily = arrayListOf()
        mHistoryMonthly = arrayListOf()
        adapterMonthly = AdminHistoryAdapter(applicationContext, mHistoryDisplay,this)
        rv_adminhistory_list.adapter = adapterMonthly
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ll_admin_daily_root.visibility = View.GONE
        ll_admin_monthly_root.visibility = View.GONE
        mSelectedCalender.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH), mSelectedCalender.get(Calendar.DAY_OF_MONTH),0,0,1)
        tv_admin_history_monthly_year.text = Utils.convertDate(mSelectedCalender.timeInMillis,"yyyy")
        tv_admin_history_monthly_monthly.text = Utils.convertDate(mSelectedCalender.timeInMillis,"MMMM")

        tv_admin_work_report_daily_date.text = Utils.convertDate(mSelectedCalender.timeInMillis, "dd MMM yyyy")
    }

    private fun setClick() {

        spnr_admin_work_report_user.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
                userSalary = mUserList[position].salary
                if(isDailyClicked)
                {
                    loadDaily()
                }
                else
                {
                    loadMonthly()
                }
            }

        }
        tv_admin_history_daily.setOnClickListener { loadDaily() }
        tv_admin_history_monthly.setOnClickListener { loadMonthly() }
        tv_admin_work_report_daily_date.setOnClickListener { loadCalendar(0) }
        tv_admin_history_monthly_year.setOnClickListener { loadCalendar(1) }
        tv_admin_history_monthly_monthly.setOnClickListener { loadCalendar(1) }
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
                            data.salary = document.data[Constants.USER_SALARY].toString().toInt()
                            mUserList.add(data)

                        }

                        userAdapter = AdminSpnrUserAdapter(this, mUserList, this)
                        spnr_admin_work_report_user.adapter = userAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())

                    }
                }


    }

    private fun loadDaily() {
        isDailyClicked = true
        ll_admin_daily_root.visibility = View.VISIBLE
        tv_admin_history_daily.setTextColor(ContextCompat.getColor(this, R.color.text_clicked))
        ll_admin_monthly_root.visibility = View.GONE
        tv_admin_history_monthly.setTextColor(ContextCompat.getColor(this, R.color.white))


        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()

        val mCalender1 = Calendar.getInstance()
        mCalender1.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH), 1,0,0,1)
        val mCalender2 = Calendar.getInstance()
        mCalender2.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH) , 1,23,59,59)


        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .whereEqualTo(Constants.CHECKIN_USEREMAIL, user!!.emailId)
                .whereGreaterThanOrEqualTo(Constants.CHECKIN_CHECKIN, mCalender1.time)
                .whereLessThanOrEqualTo(Constants.CHECKIN_CHECKIN, mCalender2.time)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        var total_payment = 0
                        var total_hours: Long = 0
                        for (document in fetchall_task.result) {
                            Log.d(" data", document.id + " => " + document.data)
                            val mCheckInData = CurrentCheckIndata()
                            mCheckInData.documentid = document.id
                            mCheckInData.siteid = document.data[Constants.CHECKIN_SITE].toString()
                            mCheckInData.sitename = document.data[Constants.CHECKIN_SITENAME].toString()
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKIN].toString()) && !document.data[Constants.CHECKIN_CHECKIN].toString().equals("null")) {
                                mCheckInData.checkintime = getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time.toLong()
                            }
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKOUT].toString()) && !document.data[Constants.CHECKIN_CHECKOUT].toString().equals("null")) {
                                mCheckInData.checkouttime = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time.toLong()
                            }
                            mCheckInData.useremail = document.data[Constants.CHECKIN_USEREMAIL].toString()
                            mCheckInData.username = document.data[Constants.CHECKIN_USERNAME].toString()
                            mCheckInData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()

                            //if (mCheckInData.checkintime!! >= mSelectedCalender.timeInMillis && mCheckInData.checkintime!! <= (mSelectedCalender.timeInMillis + (24L * 60L * 60L * 1000L))) {
                                if (!mCheckInData.payment.equals("null") && mCheckInData.payment.toString() != "") {
                                    val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                    total_payment += mPayment
                                }
                                if (mCheckInData.checkintime != 0L) {
                                    if (mCheckInData.checkouttime != 0L) {
                                        val mHours = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time - getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                                        total_hours += (mHours)
                                    }
                                }
                                mHistoryDaily.add(mCheckInData)
                           // }

                        }
                        tv_admin_history_daily_payment.text = getString(R.string.rupees, total_payment)

                        if (total_hours > 0) {

                            tv_admin_work_report_daily_total_hours.text = Utils.convertHours(total_hours)

                        } else {
                            tv_admin_work_report_daily_total_hours.text = getString(R.string.not_checked_out)
                        }


                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())
                        Log.e("With time", fetchall_task.exception.toString())
                    }
                }
    }

    private fun loadMonthly() {
        isDailyClicked = false
        ll_admin_daily_root.visibility = View.GONE
        tv_admin_history_daily.setTextColor(ContextCompat.getColor(this, R.color.white))
        ll_admin_monthly_root.visibility = View.VISIBLE
        tv_admin_history_monthly.setTextColor(ContextCompat.getColor(this, R.color.text_clicked))

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()

        mHistoryMonthly.clear()
        mHistoryDisplay.clear()
        val mCalender1 = Calendar.getInstance()
        mCalender1.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH), 1,0,0,1)
        val mCalender2 = Calendar.getInstance()
        mCalender2.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH) + 1, 1,23,59,59)



        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .whereEqualTo(Constants.CHECKIN_USEREMAIL, user!!.emailId)
                .whereGreaterThanOrEqualTo(Constants.CHECKIN_CHECKIN, mCalender1.time)
                .whereLessThanOrEqualTo(Constants.CHECKIN_CHECKIN, mCalender2.time)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        var total_payment = 0
                        var total_hours: Long = 0
                        for (document in fetchall_task.result) {
                            Log.d(" data", document.id + " => " + document.data)

                            val mCheckInData = CurrentCheckIndata()
                            mCheckInData.documentid = document.id
                            mCheckInData.siteid = document.data[Constants.CHECKIN_SITE].toString()
                            mCheckInData.sitename = document.data[Constants.CHECKIN_SITENAME].toString()
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKIN].toString()) && !document.data[Constants.CHECKIN_CHECKIN].toString().equals("null")) {
                                mCheckInData.checkintime = getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time.toLong()
                            }
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKOUT].toString()) && !document.data[Constants.CHECKIN_CHECKOUT].toString().equals("null")) {
                                mCheckInData.checkouttime = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time.toLong()
                            }
                            mCheckInData.useremail = document.data[Constants.CHECKIN_USEREMAIL].toString()
                            mCheckInData.username = document.data[Constants.CHECKIN_USERNAME].toString()
                            mCheckInData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()

                           // if (mCheckInData.checkintime!! >= (mSelectedCalender.timeInMillis - (mSelectedCalender.get(Calendar.DAY_OF_MONTH) * 24L * 60L * 60L * 1000L)) && mCheckInData.checkintime!! <= (mSelectedCalender.timeInMillis + ((mSelectedCalender.getActualMaximum(Calendar.DATE) - mSelectedCalender.get(Calendar.DAY_OF_MONTH))* 24L * 60L * 60L * 1000L))) {
                                if (!document.data[Constants.CHECKIN_PAYMENT].toString().equals("null") && !document.data[Constants.CHECKIN_PAYMENT].toString().equals("")) {
                                    val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                    total_payment += mPayment
                                }
                                if (mCheckInData.checkintime != 0L) {
                                    if (mCheckInData.checkouttime != 0L) {
                                        val mHours = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time - getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                                        total_hours += (mHours)
                                    }
                            //    }
                                mHistoryMonthly.add(mCheckInData)
                            }

                        }
                        tv_admin_history_monthly_payment.text = getString(R.string.rupees, total_payment)

                        if (total_hours > 0) {

                            tv_admin_history_monthly_total_hours.text = Utils.convertHours(total_hours)

                        } else {
                            tv_admin_history_monthly_total_hours.text = getString(R.string.not_checked_out)
                        }


                        mHistoryDisplay.addAll(mHistoryMonthly)
                        adapterMonthly.notifyDataSetChanged()
                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())
                        Log.e("With time", fetchall_task.exception.toString())
                    }
                }
    }


    private fun loadCalendar(from : Int) {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = android.app.DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    mSelectedCalender.set(year, monthOfYear, dayOfMonth,0,0,1)
                   if(from == 0)
                   {
                       tv_admin_work_report_daily_date.text = Utils.convertDate(mSelectedCalender.timeInMillis, "dd MMM yyyy")
                       loadDaily()
                   }

                    else
                   {
                       tv_admin_history_monthly_year.text = Utils.convertDate(mSelectedCalender.timeInMillis,"yyyy")
                       tv_admin_history_monthly_monthly.text = Utils.convertDate(mSelectedCalender.timeInMillis,"MMMM")

                       loadMonthly()
                   }

                }, mYear, mMonth, mDay)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000


        datePickerDialog.setTitle(null)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }

    private fun getDate(date: String): Date {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val value: Date = format.parse(date)
        return value
    }
    private fun loadEdit(data: CurrentCheckIndata)
    {

        val intent = Intent(this@AdminWorkReportsActivity, EditWorkReportActivity::class.java)
        intent.putExtra("data",data)
        startActivity(intent)
    }
}
