package net.appitiza.workmanager.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import net.appitiza.workmanager.R
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


open class BaseActivity : AppCompatActivity() {

    fun showValidationWarning(message: String) {
        val mAlert = AlertDialog.Builder(this).create()
        mAlert.setTitle(getString(R.string.app_name))
        mAlert.setMessage(message)
        mAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, which ->

            mAlert.dismiss()
        })

        mAlert.show()

    }
    fun hideKeyboard()
    {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
