package com.example.eshop.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.eshop.R
import com.example.eshop.databinding.ActivityAddEditAddressBinding
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.Address
import com.example.eshop.utils.Constants
import com.example.eshop.utils.ProgressBarUtil
import com.example.eshop.utils.SnackBarUtil

class AddEditAddressActivity : AppCompatActivity() {

    private var addressDetails: Address? = null
    private lateinit var binding: ActivityAddEditAddressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)) {
            addressDetails =
                intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)!!
        }

        setupActionBar()

        if (addressDetails != null) {
            if (addressDetails!!.id.isNotEmpty()) {

                binding.apply {
                    tvTitle.text = resources.getString(R.string.title_edit_address)
                    btnSubmitAddress.text = resources.getString(R.string.btn_lbl_update)

                    etFullName.setText(addressDetails?.name)
                    etPhoneNumber.setText(addressDetails?.mobileNumber)
                    etAddress.setText(addressDetails?.address)
                    etZipCode.setText(addressDetails?.zipCode)
                    etAdditionalNote.setText(addressDetails?.additionalNote)
                }

                when (addressDetails?.type) {
                    Constants.HOME -> {
                        binding.rbHome.isChecked = true
                    }

                    Constants.OFFICE -> {
                        binding.rbOffice.isChecked = true
                    }

                    else -> {
                        binding.rbOther.isChecked = true
                        binding.tilOtherDetails.visibility = View.VISIBLE
                        binding.etOtherDetails.setText(addressDetails?.otherDetails)
                    }
                }
            }
        }

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_other) {
                binding.tilOtherDetails.visibility = View.VISIBLE
            } else {
                binding.tilOtherDetails.visibility = View.GONE
            }
        }

        binding.btnSubmitAddress.setOnClickListener {
            saveAddressToFirestore()
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarAddEditAddressActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding.toolbarAddEditAddressActivity.setNavigationOnClickListener { onBackPressed() }
    }


    /**
     * A function to validate the address input entries.
     */
    private fun validateData(): Boolean {
        val rootView: View = findViewById(android.R.id.content)
        return when {

            TextUtils.isEmpty(binding.etFullName.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_please_enter_full_name),
                    true
                )
                false
            }

            TextUtils.isEmpty(binding.etPhoneNumber.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_please_enter_phone_number),
                    true
                )
                false
            }

            TextUtils.isEmpty(binding.etAddress.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_please_enter_address),
                    true
                )
                false
            }

            TextUtils.isEmpty(binding.etZipCode.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_please_enter_zip_code),
                    true
                )
                false
            }

            binding.rbOther.isChecked && TextUtils.isEmpty(
                binding.etZipCode.text.toString().trim { it <= ' ' }) -> {
                SnackBarUtil.showSnackBar(
                    this,
                    rootView,
                    resources.getString(R.string.err_msg_please_enter_zip_code),
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
     * A function to save the address to the cloud firestore.
     */
    private fun saveAddressToFirestore() {

        // Here we get the text from editText and trim the space
        val fullName: String = binding.etFullName.text.toString().trim { it <= ' ' }
        val phoneNumber: String = binding.etPhoneNumber.text.toString().trim { it <= ' ' }
        val address: String = binding.etAddress.text.toString().trim { it <= ' ' }
        val zipCode: String = binding.etZipCode.text.toString().trim { it <= ' ' }
        val additionalNote: String = binding.etAdditionalNote.text.toString().trim { it <= ' ' }
        val otherDetails: String = binding.etOtherDetails.text.toString().trim { it <= ' ' }

        if (validateData()) {

            // Show the progress dialog.
            ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))

            val addressType: String = when {
                binding.rbHome.isChecked -> {
                    Constants.HOME
                }

                binding.rbOffice.isChecked -> {
                    Constants.OFFICE
                }

                else -> {
                    Constants.OTHER
                }
            }

            val addressModel = Address(
                FireStoreHandler().getCurrentUserId(),
                fullName,
                phoneNumber,
                address,
                zipCode,
                additionalNote,
                addressType,
                otherDetails
            )

            if (addressDetails != null && addressDetails!!.id.isNotEmpty()) {
                FireStoreHandler().updateAddress(
                    this@AddEditAddressActivity,
                    addressModel,
                    addressDetails!!.id
                )
            } else {
                FireStoreHandler().addAddress(this@AddEditAddressActivity, addressModel)
            }
        }
    }

    /**
     * A function to notify the success result of address saved.
     */
    fun onAddUpdateAddressSuccess() {

        // Hide progress dialog
        ProgressBarUtil.hideProgressBar()

        val notifySuccessMessage: String =
            if (addressDetails != null && addressDetails!!.id.isNotEmpty()) {
                resources.getString(R.string.msg_your_address_updated_successfully)
            } else {
                resources.getString(R.string.err_your_address_added_successfully)
            }

        Toast.makeText(
            this@AddEditAddressActivity,
            notifySuccessMessage,
            Toast.LENGTH_SHORT
        ).show()

        // TODO Step 13: Now se the result to OK.
        setResult(RESULT_OK)
        finish()
    }
}