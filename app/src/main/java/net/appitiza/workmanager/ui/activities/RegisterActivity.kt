package net.appitiza.workmanager.ui.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_admin_sites.*
import kotlinx.android.synthetic.main.activity_register.*
import net.appitiza.workmanager.BuildConfig
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.users.UsersActivity
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.util.*


class RegisterActivity : BaseActivity() {
    private var mProgress: ProgressDialog? = null

    //Firebase auth
    private var mAuth: FirebaseAuth? = null
    //Firestore referrence
    private lateinit var db: FirebaseFirestore

    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var salary by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_SALARY, 0)
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 33
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initializeFireBase()
        tv_register_register.setOnClickListener {
            if (checkPermissions()) {
                registerUser(et_register_name.text.toString(), et_register_email.text.toString(), et_register_password.text.toString())
            } else {
                requestPermissions()
            }
        }
    }

    private fun initializeFireBase() {
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun registerUser(displayname: String, email: String, password: String) {
        if (validation(displayname, email, password)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.registering_message))
            mProgress?.setCancelable(false)
            mProgress?.show()
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val device_id = tm.deviceId
            mAuth?.createUserWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth?.currentUser
                            val uid = user!!.uid
                            val deviceToken: String? = FirebaseInstanceId.getInstance().token
                            val map = HashMap<String, Any>()
                            map[Constants.USER_DISPLAY_NAME] = displayname
                            map[Constants.USER_EMAIL] = mAuth?.currentUser?.email.toString()
                            map[Constants.USER_TOKEN] = deviceToken.toString()
                            map[Constants.USER_IMEI] = device_id
                            map[Constants.USER_SALARY] = "0"
                            map[Constants.USER_TYPE] = "user"
                            map[Constants.USER_IMAGE] = "default"
                            map[Constants.USER_THUMB] = "default"
                            map[Constants.USER_STATUS] = "HI! Iam using Work Manager"
                            map[Constants.USER_LASTSEEN] = FieldValue.serverTimestamp()
                            map[Constants.USER_REG_TIME] = FieldValue.serverTimestamp()



                            db.collection(Constants.COLLECTION_USER)
                                    .document(mAuth?.currentUser?.email.toString())
                                    .set(map, SetOptions.merge())
                                    .addOnCompleteListener { reg_task ->
                                        if (reg_task.isSuccessful) {
                                            mProgress!!.dismiss()
                                            useremail = mAuth?.currentUser?.email.toString()
                                            isLoggedIn = true
                                            this.displayName = displayname
                                            userpassword = password
                                            usertype = "user"
                                            startActivity(Intent(this@RegisterActivity, UsersActivity::class.java))
                                            finish()

                                        } else {
                                            mProgress!!.hide()

                                            Utils.showDialog(this, reg_task.exception?.message.toString())
                                        }
                                    }


                        } else {
                            mProgress!!.hide()
                            Utils.showDialog(this, task.exception?.message.toString())
                        }
                    }
        } else {
            Utils.showDialog(this, "Incomplete Entry")
        }
    }

    private fun validation(displayname: String, email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(displayname)) {
            showValidationWarning(getString(R.string.name_missing))
            false
        } else if (TextUtils.isEmpty(email)) {
            showValidationWarning(getString(R.string.email_missing))
            false
        } else if (TextUtils.isEmpty(password)) {
            showValidationWarning(getString(R.string.password_missing))
            false
        } else if (password.length < 6) {
            showValidationWarning(getString(R.string.password_length_missing))
            false
        } else {
            true
        }
    }

    private fun checkPermissions(): Boolean {
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
                Manifest.permission.READ_PHONE_STATE)

        if (shouldProvideRationale) {
            Snackbar.make(
                    fab_admin_add_site,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, View.OnClickListener {
                        requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    })
                    .show()
        } else {

            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQUEST_PERMISSIONS_REQUEST_CODE)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                registerUser(et_register_name.text.toString(), et_register_email.text.toString(), et_register_password.text.toString())

            } else {
                // Permission denied.
                Snackbar.make(fab_admin_add_site,
                        R.string.permission_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, View.OnClickListener {
                            val intent: Intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
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
}
