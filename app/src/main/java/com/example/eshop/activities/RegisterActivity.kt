package com.example.eshop.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.eshop.R
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.User
import com.example.eshop.utils.ProgressBarUtil
import com.example.eshop.utils.SnackBarUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var et_first_name: EditText
    private lateinit var et_last_name: EditText
    private lateinit var et_password: EditText
    private lateinit var et_confirm_password: EditText
    private lateinit var et_email: EditText
    private lateinit var cb_terms_and_condition: CheckBox
    private lateinit var rootView: View
    private lateinit var loginTV: TextView
    private lateinit var registerBtn: Button
//    private lateinit var progressBar: Dialog

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setUpActionBar()
        viewsInitialization()

        loginTV.setOnClickListener {
            onBackPressed()
        }

        registerBtn.setOnClickListener {
            validateRegisterDetails()
        }
    }

    private fun setUpActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_register_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun viewsInitialization() {
        et_first_name = findViewById(R.id.et_first_name)
        et_last_name = findViewById(R.id.et_last_name)
        et_email = findViewById(R.id.et_email)
        et_password = findViewById(R.id.et_password)
        et_confirm_password = findViewById(R.id.et_confirm_password)
        cb_terms_and_condition = findViewById(R.id.cb_terms_and_condition)
        rootView = findViewById(android.R.id.content)
        loginTV = findViewById(R.id.tv_login)
        registerBtn = findViewById(R.id.btn_register)

        auth = Firebase.auth
    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    view = rootView,
                    resources.getString(R.string.err_msg_enter_first_name),
                    true
                )
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_enter_last_name),
                    true
                )
                false
            }

            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_enter_email),
                    true
                )
                false
            }

            TextUtils.isEmpty(
                et_password.text.toString()
                    .trim { it <= ' ' }) || et_password.text.toString().trim().length < 6 -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_enter_password),
                    true
                )
                false
            }

            TextUtils.isEmpty(
                et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this, rootView,
                    resources.getString(R.string.err_msg_enter_confirm_password),
                    true
                )
                false
            }

            et_password.text.toString().trim { it <= ' ' } != et_confirm_password.text.toString()
                .trim { it <= ' ' } -> {
                SnackBarUtil.showSnackBar(
                    this, rootView,
                    resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                    true
                )
                false
            }

            !cb_terms_and_condition.isChecked -> {
                SnackBarUtil.showSnackBar(
                    this, rootView,
                    resources.getString(R.string.err_msg_agree_terms_and_condition),
                    true
                )
                false
            }

            else -> {
                registerUser()
                true
            }
        }
    }

    private fun registerUser() {
        val email = et_email.text.toString().trim { it <= ' ' }
        val password = et_password.text.toString().trim { it <= ' ' }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                val user = User(
                    firebaseUser!!.uid,
                    et_first_name.text.toString().trim(),
                    et_last_name.text.toString().trim(),
                    et_email.text.toString().trim()
                )
                FireStoreHandler().registerUser(this, user)

            } else {
                ProgressBarUtil.hideProgressBar()
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    "Authentication Failed.",
                    true
                )
            }
        }

    }

    fun onUserRegistrationSuccess() {
        //            to hide progress bar after successful creating user or displaying error
        ProgressBarUtil.hideProgressBar()
        Toast.makeText(this, resources.getString(R.string.register_success), Toast.LENGTH_LONG)
            .show()
        //                sign out after successfully registering and go back to login activity
        auth.signOut()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}