package com.example.eshop.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.eshop.R
import com.example.eshop.databinding.ActivityProductDetailsBinding
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.CartItem
import com.example.eshop.models.Product
import com.example.eshop.utils.Constants
import com.example.eshop.utils.GlideImageLoader
import com.example.eshop.utils.ProgressBarUtil

class ProductDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityProductDetailsBinding
    private var productId: String = ""
    private var productOwnerId = ""

    private lateinit var productDetails: Product


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            productId =
                intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }


        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            productOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }

        if (FireStoreHandler().getCurrentUserId() == productOwnerId) {
            binding.btnAddToCart.visibility = View.GONE
        } else {
            binding.btnAddToCart.visibility = View.VISIBLE
        }

        setupActionBar()

        if (FireStoreHandler().getCurrentUserId() == productOwnerId) {
            binding.btnAddToCart.visibility = View.GONE
            binding.btnGoToCart.visibility = View.GONE
        } else {
            binding.btnAddToCart.visibility = View.VISIBLE
        }

        binding.btnAddToCart.setOnClickListener(this)
        binding.btnGoToCart.setOnClickListener(this)

        getProductDetails()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.btn_add_to_cart -> {
                    addToCart()
                }

                R.id.btn_go_to_cart -> {
                    startActivity(Intent(this@ProductDetailsActivity, CartListActivity::class.java))
                }
            }
        }
    }

    private fun addToCart() {

        val addToCart = CartItem(
            FireStoreHandler().getCurrentUserId(),
            productOwnerId,
            productId,
            productDetails.title,
            productDetails.price,
            productDetails.image,
            Constants.DEFAULT_CART_QUANTITY
        )

        // Show the progress dialog
        ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))

        FireStoreHandler().addCartItems(this@ProductDetailsActivity, addToCart)
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarProductDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding.toolbarProductDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to call the firestore class function that will get the product details from cloud firestore based on the product id.
     */
    private fun getProductDetails() {

        // Show the product dialog
        ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))

        // Call the function of FirestoreClass to get the product details.
        FireStoreHandler().getProductDetails(this@ProductDetailsActivity, productId)
    }

    /**
     * A function to notify the success result of the product details based on the product id.
     *
     * @param product A model class with product details.
     */
    fun onProductDetailsSuccess(product: Product) {

        productDetails = product
        // Hide Progress dialog.
        ProgressBarUtil.hideProgressBar()

        // Populate the product details in the UI.
        GlideImageLoader.loadProductPicture(
            this,
            product.image,
            binding.ivProductDetailImage
        )

        binding.apply {
            tvProductDetailsTitle.text = product.title
            tvProductDetailsPrice.text = "$${product.price}"
            tvProductDetailsDescription.text = product.description
            tvProductDetailsStockQuantity.text = product.stock_quantity
        }

        if (product.stock_quantity.toInt() == 0) {

            // Hide Progress dialog.
            ProgressBarUtil.hideProgressBar()

            // Hide the AddToCart button if the item is already in the cart.
            binding.btnAddToCart.visibility = View.GONE

            binding.tvProductDetailsStockQuantity.text =
                resources.getString(R.string.lbl_out_of_stock)

            binding.tvProductDetailsStockQuantity.setTextColor(
                ContextCompat.getColor(
                    this@ProductDetailsActivity,
                    R.color.colorSnackBarError
                )
            )
        } else {

            // There is no need to check the cart list if the product owner himself is seeing the product details.
            if (FireStoreHandler().getCurrentUserId() == product.user_id) {
                // Hide Progress dialog.
                ProgressBarUtil.hideProgressBar()
            } else {
                FireStoreHandler().checkIfItemExistInCart(this@ProductDetailsActivity, productId)
            }
        }
    }

    /**
     * A function to notify the success result of item exists in the cart.
     */
    fun productExistsInCart() {

        // Hide the progress dialog.
        ProgressBarUtil.hideProgressBar()

        // Hide the AddToCart button if the item is already in the cart.
        binding.btnAddToCart.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        binding.btnGoToCart.visibility = View.VISIBLE
    }

    /**
     * A function to notify the success result of item added to the to cart.
     */
    fun addToCartSuccess() {
        // Hide the progress dialog.
        ProgressBarUtil.hideProgressBar()

        Toast.makeText(
            this@ProductDetailsActivity,
            resources.getString(R.string.success_message_item_added_to_cart),
            Toast.LENGTH_SHORT
        ).show()

        // Hide the AddToCart button if the item is already in the cart.
        binding.btnAddToCart.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        binding.btnGoToCart.visibility = View.VISIBLE
    }
}