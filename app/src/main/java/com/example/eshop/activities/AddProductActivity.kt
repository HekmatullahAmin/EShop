package com.example.eshop.activities


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils

import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat

import com.example.eshop.R
import com.example.eshop.databinding.ActivityAddProductBinding
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.Product
import com.example.eshop.utils.Constants
import com.example.eshop.utils.GlideImageLoader
import com.example.eshop.utils.ProgressBarUtil
import com.example.eshop.utils.SnackBarUtil

class AddProductActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private var productImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar()
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageData = result.data
                    if (imageData != null) {
                        selectedImageUri = imageData.data
                        if (selectedImageUri != null) {
                            GlideImageLoader.loadUserPicture(
                                this,
                                selectedImageUri!!,
                                binding.ivProductImage
                            )
                        }
                    } else {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        binding.ivAddUpdateProduct.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    private fun setSupportActionBar() {
        setSupportActionBar(binding.toolbarAddProductActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding.toolbarAddProductActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivAddUpdateProduct -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
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
                if (validateProductDetails()) {
                    uploadProductImage()
                }
            }
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(galleryIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    private fun validateProductDetails(): Boolean {
        val rootView: View = findViewById<View>(android.R.id.content)
        return when {
            selectedImageUri == null -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_select_product_image),
                    true
                )
                false
            }

            TextUtils.isEmpty(binding.etProductTitle.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_enter_product_title),
                    true
                )
                false
            }

            TextUtils.isEmpty(binding.etProductPrice.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_enter_product_price),
                    true
                )
                false
            }

            TextUtils.isEmpty(binding.etProductDescription.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_enter_product_description),
                    true
                )
                false
            }

            TextUtils.isEmpty(binding.etProductQuantity.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_enter_product_quantity),
                    true
                )
                false
            }

            else -> {
                true
            }
        }
    }

    /**
     * A function to upload the selected product image to firebase cloud storage.
     */
    private fun uploadProductImage() {

        ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))

        FireStoreHandler().uploadImageToCloudStorage(
            this@AddProductActivity,
            selectedImageUri,
            Constants.PRODUCT_IMAGE
        )
    }

    /**
     * A function to get the successful result of product image upload.
     */
    fun onImageUploadSuccess(imageURL: String) {
//         Initialize the global image url variable.
        productImageUrl = imageURL

        uploadProductDetails()
    }

    private fun uploadProductDetails() {

        // Get the logged in username from the SharedPreferences that we have stored at a time of login.
        val username =
            this.getSharedPreferences(Constants.ESHOP_PREFERENCES, Context.MODE_PRIVATE)
                .getString(Constants.LOGGED_IN_USER, "")!!

        // Here we get the text from editText and trim the space
        val product = Product(
            FireStoreHandler().getCurrentUserId(),
            username,
            binding.etProductTitle.text.toString().trim(),
            binding.etProductPrice.text.toString().trim(),
            binding.etProductDescription.text.toString().trim(),
            binding.etProductQuantity.text.toString().trim(),
            productImageUrl
        )

        FireStoreHandler().uploadProductDetails(this@AddProductActivity, product)
    }

    /**
     * A function to return the successful result of Product upload.
     */
    fun productUploadSuccess() {

        // Hide the progress dialog
        ProgressBarUtil.hideProgressBar()

        Toast.makeText(
            this@AddProductActivity,
            resources.getString(R.string.product_uploaded_success_message),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}