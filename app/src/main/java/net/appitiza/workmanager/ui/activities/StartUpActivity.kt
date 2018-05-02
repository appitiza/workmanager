package net.appitiza.workmanager.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_start_up.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.admin.AdminActivity
import net.appitiza.workmanager.ui.activities.users.UsersActivity
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils


class StartUpActivity : BaseActivity() {

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
    private var userimei by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_IMEI, "")
    private var salary by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_SALARY, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_up)
        initialize()
        setClick()

    }

    private fun initialize() {
        mProgress = ProgressDialog(this)
        //Firebase auth
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setClick() {
        tv_login_login.setOnClickListener {

            loginUser(et_login_email.text.toString(), et_login_password.text.toString())
        }
        tv_login_reset_password.setOnClickListener { resetPassword(et_login_email.text.toString()) }
        tv_login_register.setOnClickListener {


            val intent = Intent(this@StartUpActivity, RegisterActivity::class.java)

            val p1 = Pair(tv_email_txt as View, getString(R.string.emailtext_login_register))
            val p2 = Pair(et_login_email as View, getString(R.string.email_login_register))
            val p3 = Pair(tv_password_txt as View, getString(R.string.passwordtext_login_register))
            val p4 = Pair(et_login_password as View, getString(R.string.password_login_register))
            val p5 = Pair(tv_login_register as View, getString(R.string.register_login_register))
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@StartUpActivity, p1, p2, p3, p4, p5)
            startActivity(intent, options.toBundle())
        }
    }

    private fun resetPassword(email: String) {
        if (!TextUtils.isEmpty(email)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.reset_message))
            mProgress?.setCancelable(false)
            mProgress?.show()
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener { resetTask ->

                mProgress?.dismiss()
                if (resetTask.isSuccessful) {

                    Utils.showDialog(this, "reset link sent")
                } else {

                    Utils.showDialog(this, resetTask.exception!!.message.toString())
                }
            }
        } else {

            Utils.showDialog(this, "email missing")
        }

    }

    private fun loginUser(email: String, password: String) {
        if (validation(email, password)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.loginin_message))
            mProgress?.setCancelable(false)
            mProgress?.show()

            mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this)
            { auth_task ->
                if (auth_task.isSuccessful) {


                    db.collection(Constants.COLLECTION_USER)
                            .document(email)
                            .get()
                            .addOnCompleteListener { login_task ->
                                if (login_task.isSuccessful) {
                                    val document = login_task.result
                                    if (document.exists()) {
                                        useremail = mAuth?.currentUser?.email.toString()
                                        isLoggedIn = true
                                        displayName = document.data[Constants.USER_DISPLAY_NAME].toString()
                                        userpassword = password
                                        usertype = document.data[Constants.USER_TYPE].toString()
                                        userimei = document.data[Constants.USER_IMEI].toString()
                                        salary = document.data[Constants.USER_SALARY].toString().toInt()
                                        mProgress?.dismiss()
                                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@StartUpActivity, tv_login_login,
                                                ViewCompat.getTransitionName(tv_login_login))
                                        et_login_email.text.clear()
                                        et_login_password.text.clear()
                                        if (usertype == "user") {
                                            val intent = Intent(this@StartUpActivity, UsersActivity::class.java)

                                            startActivity(intent, options.toBundle())
                                        } else {
                                            val intent = Intent(this@StartUpActivity, AdminActivity::class.java)

                                            startActivity(intent, options.toBundle())
                                        }


                                    } else {
                                        mProgress?.dismiss()
                                        Utils.showDialog(this, "Invalid Profile")
                                    }

                                } else {
                                    mProgress?.dismiss()
                                    Utils.showDialog(this, login_task.exception!!.message.toString())

                                }
                            }
                } else {
                    mProgress?.hide()

                    Utils.showDialog(this, auth_task.exception!!.message.toString())
                }
            }
        } else {
            Utils.showDialog(this, "Email or Password is missing")
        }
    }

    private fun validation(email: String, password: String): Boolean {
        return if (email == "") {
            showValidationWarning(getString(R.string.email_missing))
            false
        } else if (password == "") {
            showValidationWarning(getString(R.string.password_missing))
            false
        } else {
            true
        }
    }

}
