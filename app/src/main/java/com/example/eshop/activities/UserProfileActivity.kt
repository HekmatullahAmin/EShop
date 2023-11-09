package com.example.eshop.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.eshop.R
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.User
import com.example.eshop.utils.Constants
import com.example.eshop.utils.GlideImageLoader
import com.example.eshop.utils.ProgressBarUtil
import com.example.eshop.utils.SnackBarUtil
import com.example.eshop.widgets.MyButton
import com.example.eshop.widgets.MyEditText
import com.example.eshop.widgets.MyRadioButton

class UserProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var firstNameET: MyEditText
    private lateinit var lastNameET: MyEditText
    private lateinit var emailET: MyEditText
    private lateinit var mobileNoET: MyEditText
    private lateinit var profilePhoto: ImageView
    private lateinit var saveButton: MyButton
    private lateinit var maleRadioBtn: MyRadioButton
    private lateinit var femaleRadioBtn: MyRadioButton
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private var userProfileImageUrl: String = ""
    private lateinit var toolbarTV: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var userDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        viewsInitialization()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
//        userDetails = User()
        if (intent.hasExtra(Constants.USER_DETAILS)) {
            userDetails = intent.getParcelableExtra(Constants.USER_DETAILS)!!
        }

        firstNameET.setText(userDetails.firstName)
        lastNameET.setText(userDetails.lastName)
        emailET.setText(userDetails.email)
        emailET.isEnabled = false

        if (userDetails.profileCompleted == 0) {
            toolbarTV.text = resources.getString(R.string.title_complete_profile)
            firstNameET.isEnabled = false
            lastNameET.isEnabled = false
        } else {
            setUpActionBar()
            toolbarTV.text = resources.getString(R.string.title_edit_profile)
            GlideImageLoader.loadUserPicture(this, userDetails.image, profilePhoto)
            if (userDetails.mobile != 0L) {
                mobileNoET.setText(userDetails.mobile.toString())
            }
            if (userDetails.gender == MALE) {
                maleRadioBtn.isChecked = true
            } else {
                femaleRadioBtn.isChecked = true
            }
        }


        profilePhoto.setOnClickListener(this)
        saveButton.setOnClickListener(this)
    }

    private fun viewsInitialization() {
        firstNameET = findViewById(R.id.et_first_name)
        lastNameET = findViewById(R.id.et_last_name)
        emailET = findViewById(R.id.et_email)
        profilePhoto = findViewById(R.id.iv_user_photo)
        mobileNoET = findViewById(R.id.et_mobile_number)
        saveButton = findViewById(R.id.btn_submit)
        maleRadioBtn = findViewById(R.id.rb_male)
        femaleRadioBtn = findViewById(R.id.rb_female)
        toolbarTV = findViewById(R.id.tv_title)
        toolbar = findViewById(R.id.toolbar_user_profile_activity)

//        Initialize the ActivityResultLauncher here
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageData = result.data
                    if (imageData != null) {
                        selectedImageUri = imageData.data!!
                        if (selectedImageUri != null) {
                            GlideImageLoader.loadUserPicture(
                                this,
                                selectedImageUri!!,
                                profilePhoto
                            )
                        }
                    }
                } else {
                    //if result is not ok we can add else block if we want to
                    // Handle the case when the user cancels the image selection
                    // You can show a message or take any appropriate action
                    Toast.makeText(
                        this,
                        resources.getString(R.string.image_selection_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    companion object {
        const val GENDER = "gender"
        const val MALE = "male"
        const val FEMALE = "female"
        const val MOBILE = "mobile"
        const val IMAGE = "image"
        const val PROFILE_COMPLETED = "profileCompleted"
        const val FIRST_NAME: String = "firstName"
        const val LAST_NAME: String = "lastName"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_user_photo -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //                    if we already have permission then show gallery directly
                        showImageChooser()
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE
                        )
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        showImageChooser()
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE
                        )
                    }
                }
            }

            R.id.btn_submit -> {
                if (validateUserProfileDetails()) {

                    ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))
                    if (selectedImageUri != null) {
                        FireStoreHandler().uploadImageToCloudStorage(
                            this,
                            selectedImageUri,
                            Constants.USER_PROFILE_IMAGE
                        )
                    } else {
                        updateProfileDetails()
                    }
                }
            }
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(galleryIntent)
    }

    private fun validateUserProfileDetails(): Boolean {
        return when {
            TextUtils.isEmpty(mobileNoET.text.toString().trim()) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    findViewById(android.R.id.content),
                    resources.getString(R.string.err_msg_enter_mobile_number),
                    true
                )
                false
            }

            else -> {
                true
            }
        }
    }

    private fun updateProfileDetails() {
        val mobileNumber = mobileNoET.text.toString().trim()
        val firstName = firstNameET.text.toString().trim()
        val lastName = lastNameET.text.toString().trim()
        val userHashMap = HashMap<String, Any>()

        val gender = if (maleRadioBtn.isChecked) {
            MALE
        } else {
            FEMALE
        }

        if (firstName != userDetails.firstName) {
            userHashMap[FIRST_NAME] = firstName
        }
        if (lastName != userDetails.lastName) {
            userHashMap[LAST_NAME] = lastName
        }

//        TODO doesn't take 0 as first digit
        if (mobileNumber.isNotEmpty() && mobileNumber != userDetails.mobile.toString()) {
            userHashMap[MOBILE] = mobileNumber
        }

        if (gender.isNotEmpty() && gender != userDetails.gender) {
            userHashMap[GENDER] = gender
        }

//        TODO If user dont select an image it will crush
        if (userProfileImageUrl.isNotEmpty()) {
            userHashMap[IMAGE] = userProfileImageUrl
        }
        userHashMap[MOBILE] = mobileNumber.toLong()
        userHashMap[GENDER] = gender
        if (userDetails.profileCompleted == 0) {
            userHashMap[PROFILE_COMPLETED] = 1
        }

        FireStoreHandler().updateUserDetails(this, userHashMap)
    }

    fun onUserProfileUpdateSuccess() {
        ProgressBarUtil.hideProgressBar()
        Toast.makeText(
            this,
            resources.getString(R.string.msg_profile_update_success),
            Toast.LENGTH_LONG
        ).show()

        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    fun onImageUploadSuccess(imageUrl: String) {
        userProfileImageUrl = imageUrl
        updateProfileDetails()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                If we recieve the permission for the first time also show gallery
                showImageChooser()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
        } else {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}