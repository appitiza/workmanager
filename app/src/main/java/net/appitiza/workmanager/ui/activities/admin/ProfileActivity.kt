package net.appitiza.workmanager.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.R.id.*
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.utils.PreferenceHelper
import android.content.Intent
import com.theartofdev.edmodo.cropper.CropImage


class ProfileActivity : BaseActivity() {

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
    private var userimage by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_IMAGE, "")
    private var userthumb by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_THUMB, "")
    private var userstatus by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_STATUS, "")
    private var salary by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_SALARY, 0)

    private val GALLERY_PICK = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initialize()
        getUserData()
        setClick()
    }

    private fun setClick() {
        iv_userprofile_image.setOnClickListener { openFile() }
    }

    private fun openFile() {
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Chat It"), GALLERY_PICK)
    }

    private fun initialize() {
        mProgress = ProgressDialog(this)
        //Firebase auth
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun getUserData() {
        if (userimage != "default") {
            val requestOptions = RequestOptions()
            requestOptions.placeholder(R.drawable.no_image)
            requestOptions.error(R.drawable.no_image)
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            Glide.with(applicationContext).load(userimage).apply(requestOptions).into(iv_userprofile_image)
        }
        tv_profile_displayname.text = displayName
        tv_profile_email.text = useremail
        tv_profile_status.text = userstatus


    }

    private fun validation(email: String): Boolean {
        return if (email == "") {
            showValidationWarning(getString(R.string.email_missing))
            false
        } else {
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            CropImage.activity(data?.data).setAspectRatio(1, 1).start(this);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {

                val uri = result.uri
                iv_userprofile_image.setImageURI(uri)
            }
        }
    }
}
