package com.example.eshop.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import com.example.eshop.R
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.User
import com.example.eshop.utils.Constants
import com.example.eshop.utils.ProgressBarUtil
import com.example.eshop.utils.SnackBarUtil
import com.example.eshop.widgets.MyBoldTextView
import com.example.eshop.widgets.MyButton
import com.example.eshop.widgets.MyEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var etEmail: MyEditText
    private lateinit var etPassword: MyEditText
    private lateinit var tvForgotPassword: MyBoldTextView
    private lateinit var tvRegister: MyBoldTextView
    private lateinit var loginBtn: MyButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        viewsInitialization()

    }

    private fun viewsInitialization() {
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        tvRegister = findViewById(R.id.tv_register)
        loginBtn = findViewById(R.id.btn_login)

        tvForgotPassword.setOnClickListener(this)
        tvRegister.setOnClickListener(this)
        loginBtn.setOnClickListener(this)
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

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.tv_forgot_password -> {
                    startActivity(Intent(this, ForgotPasswordActivity::class.java))
                }

                R.id.btn_login -> {
                    validateLoginDetails()
                }

                R.id.tv_register -> {
                    val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(registerIntent)
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        val rootView = findViewById<View>(android.R.id.content)
        return when {
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this, view = rootView, resources.getString(R.string.err_msg_enter_email), true
                )
                false
            }

            TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this, rootView, resources.getString(R.string.err_msg_enter_password), true
                )
                false
            }

            else -> {
                loginRegisteredUser()
                true
            }
        }
    }

    private fun loginRegisteredUser() {
        ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val auth = Firebase.auth
        val rootView = findViewById<View>(android.R.id.content)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FireStoreHandler().getUserDetails(this)
            } else {
                ProgressBarUtil.hideProgressBar()
                SnackBarUtil.showSnackBar(
                    this, rootView, task.exception?.message.toString(), true
                )
            }
        }
    }

    fun onLoggedInSuccess(user: User) {
        ProgressBarUtil.hideProgressBar()
        val action: Intent = if (user.profileCompleted == 0) {
            Intent(this, UserProfileActivity::class.java)
        } else {
            Intent(this, DashboardActivity::class.java)
        }
        action.putExtra(Constants.USER_DETAILS, user)
        startActivity(action)
        finish()
    }
}