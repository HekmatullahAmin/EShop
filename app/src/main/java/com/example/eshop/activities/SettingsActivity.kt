package com.example.eshop.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.eshop.R
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.User
import com.example.eshop.utils.Constants
import com.example.eshop.utils.GlideImageLoader
import com.example.eshop.utils.ProgressBarUtil
import com.example.eshop.widgets.MyBoldTextView
import com.example.eshop.widgets.MyButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var toolbar: Toolbar
    private lateinit var userProfile: ImageView
    private lateinit var userName: MyBoldTextView
    private lateinit var userEmail: TextView
    private lateinit var userMobileNo: TextView
    private lateinit var editTV: TextView
    private lateinit var logoutBtn: MyButton
    private lateinit var userDetails: User
    private lateinit var ll_address: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        viewsInitialization()
        setUpActionBar()
        getUserDetails()

        editTV.setOnClickListener(this)
        logoutBtn.setOnClickListener(this)
        ll_address.setOnClickListener(this@SettingsActivity)
    }

    private fun viewsInitialization() {
        toolbar = findViewById(R.id.toolbar_settings_activity)
        userProfile = findViewById(R.id.iv_user_photo)
        userName = findViewById(R.id.tv_name)
        userEmail = findViewById(R.id.tv_email)
        userMobileNo = findViewById(R.id.tv_mobile_number)
        editTV = findViewById(R.id.tv_edit)
        logoutBtn = findViewById(R.id.btn_logout)
        ll_address = findViewById(R.id.ll_address)
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getUserDetails() {
        ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))
        FireStoreHandler().getUserDetails(this)
    }

    fun onUserDetailsSuccess(user: User) {
        userDetails = user
        ProgressBarUtil.hideProgressBar()
        GlideImageLoader.loadUserPicture(this, user.image, userProfile)
        userName.text = "${user.firstName} ${user.lastName}"
        userEmail.text = user.email
        userMobileNo.text = user.mobile.toString()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_edit -> {
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra(Constants.USER_DETAILS, userDetails)
                startActivity(intent)
            }

            R.id.ll_address -> {
                val intent = Intent(this@SettingsActivity, AddressListActivity::class.java)
                startActivity(intent)
            }

            R.id.btn_logout -> {
                Firebase.auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}