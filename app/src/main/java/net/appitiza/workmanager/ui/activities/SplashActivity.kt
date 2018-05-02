package net.appitiza.workmanager.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.admin.AdminActivity
import net.appitiza.workmanager.ui.activities.users.UsersActivity
import net.appitiza.workmanager.utils.PreferenceHelper

class SplashActivity : AppCompatActivity() {

    private val delayTime: Long = 3000
    private var delayJob: Job? = null

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
    private var userimage by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_IMAGE, "")
    private var userthumb by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_THUMB, "")
    private var userstatus by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_STATUS, "")
    private var salary by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_SALARY, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initialize()
        if (isLoggedIn) {
            //Navigate with delay
            login()
        } else {

            delayJob = delaySplashScreen()
        }
    }

    public override fun onDestroy() {
        delayJob?.cancel()

        super.onDestroy()
    }

    private fun initialize() {
        //Firebase auth
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun delaySplashScreen() = launch(UI) {
        tv_loading_status.text = "Configuring.."
        async(CommonPool) { delay(delayTime) }.await()
        //isLoggedIn = true;
        val intent = Intent(this@SplashActivity, StartUpActivity::class.java)
        startActivity(intent);
        finish()
    }


    private fun login() {
        if (useremail != "" && userpassword != "") {
            loginUser(useremail, userpassword)

        } else {
            val intent = Intent(this@SplashActivity, StartUpActivity::class.java)
            startActivity(intent);
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {

        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this)
        { task ->
            if (task.isSuccessful) {
                db.collection(Constants.COLLECTION_USER)
                        /*.whereEqualTo(Constants.USER_EMAIL, mAuth?.currentUser?.email.toString())*/
                        .document(email)
                        .get()
                        .addOnCompleteListener { login_task ->
                            if (login_task.isSuccessful) {
                                val document = login_task.result
                                if (document.exists()) {
                                    useremail = mAuth?.currentUser?.email.toString()
                                    isLoggedIn = true
                                    displayName =  document.data[Constants.USER_DISPLAY_NAME].toString()
                                    userpassword = password
                                    usertype = document.data[Constants.USER_TYPE].toString()
                                    userimei = document.data[Constants.USER_IMEI].toString()
                                    userimage = document.data[Constants.USER_IMAGE].toString()
                                    userthumb = document.data[Constants.USER_THUMB].toString()
                                    userstatus = document.data[Constants.USER_STATUS].toString()
                                    salary = document.data[Constants.USER_SALARY].toString().toInt()
                                    if (usertype =="user") {
                                        val intent = Intent(this@SplashActivity, UsersActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        val intent = Intent(this@SplashActivity, AdminActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }


                                } else {
                                    val intent = Intent(this@SplashActivity, StartUpActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                            } else {
                                val intent = Intent(this@SplashActivity, StartUpActivity::class.java)
                                startActivity(intent)
                                finish()

                            }
                        }


            } else {
                val intent = Intent(this@SplashActivity, StartUpActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
    }
}

