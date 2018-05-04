package net.appitiza.workmanager.ui.activities.admin

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_profile.*
import net.appitiza.workmanager.R
import net.appitiza.workmanager.constants.Constants
import net.appitiza.workmanager.ui.activities.BaseActivity
import net.appitiza.workmanager.utils.PreferenceHelper
import net.appitiza.workmanager.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class ProfileActivity : BaseActivity() {

    private var mProgress: ProgressDialog? = null
    //Firebase auth
    private var mAuth: FirebaseAuth? = null
    //Firestore referrence
    private lateinit var db: FirebaseFirestore

    private lateinit var mstorageRef: StorageReference

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
    private lateinit var mImageuri: Uri
    private lateinit var mThumburi: Uri
    //  private  var thumb_byte : byte[]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initialize()
        getUserData()
        setClick()
    }

    private fun setClick() {
        iv_userprofile_image.setOnClickListener {
            openFile()
        }
        tv_profile_update_profile.setOnClickListener {
            updateImage(mImageuri)
        }
        tv_profile_reset_password.setOnClickListener {
            resetPassword(useremail)
        }
    }

    private fun updateImage(imageUri: Uri) {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.image_uploading))
        mProgress?.setCancelable(false)
        mProgress?.show()
        val profileimage = mstorageRef.child(Constants.STORAGE_PROFILE).child(useremail + ".jpg")
        profileimage.putFile(imageUri)
                .addOnCompleteListener { imageTask ->
                    if (imageTask.isSuccessful) {

                        val thumb_bitmap = Compressor(this)
                                .setMaxWidth(75)
                                .setMaxHeight(75)
                                .setQuality(75)
                                .compressToBitmap(File(mImageuri.path))
                        val baos = ByteArrayOutputStream()
                        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                        var thumb_byte = baos.toByteArray();

                        val thumbimage = mstorageRef.child(Constants.STORAGE_THUMB).child(useremail + ".jpg")
                        thumbimage.putBytes(thumb_byte)
                                .addOnCompleteListener { thumbTask ->
                                    mProgress?.dismiss()
                                    if (thumbTask.isSuccessful) {
                                        val thump_link = thumbTask.result.downloadUrl.toString()
                                        val image_link = imageTask.result.downloadUrl.toString()
                                        userimage = image_link
                                        userthumb = thump_link
                                        val map = HashMap<String, Any>()
                                        //map[Constants.USER_DISPLAY_NAME] = displayname
                                        map[Constants.USER_IMAGE] = image_link
                                        map[Constants.USER_THUMB] = thump_link
                                        //map[Constants.USER_STATUS] = "HI! Iam using Work Manager"
                                        updateData(useremail, map)
                                    } else {

                                        Utils.showDialog(this, thumbTask.exception!!.message.toString())
                                    }
                                }
                    } else {
                        mProgress?.dismiss()
                        Utils.showDialog(this, imageTask.exception!!.message.toString())
                    }
                }
    }

    private fun updateData(email: String, map: HashMap<String, Any>) {

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.profile_updating))
        mProgress?.setCancelable(false)
        mProgress?.show()


        db.collection(Constants.COLLECTION_USER)
                .document(email)
                .set(map, SetOptions.merge())
                .addOnCompleteListener { reg_task ->
                    if (reg_task.isSuccessful) {
                        mProgress!!.dismiss()
                        Utils.showDialog(this, getString(R.string.profile_updated).toString())

                    } else {
                        mProgress!!.hide()

                        Utils.showDialog(this, reg_task.exception?.message.toString())
                    }
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
        mstorageRef = FirebaseStorage.getInstance().reference
    }

    private fun getUserData() {
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.default_image)
        requestOptions.error(R.drawable.default_image)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.circleCrop()
        Glide.with(applicationContext).load(userimage).apply(requestOptions).into(iv_userprofile_image)
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
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {

                mImageuri = result.uri
                //iv_userprofile_image.setImageURI(mImageuri)

                val requestOptions = RequestOptions()
                requestOptions.placeholder(R.drawable.no_image)
                requestOptions.error(R.drawable.no_image)
                requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
                requestOptions.circleCrop()
                Glide.with(applicationContext).load(File(result.uri.path)).apply(requestOptions).into(iv_userprofile_image)
            }
        }
    }
}
