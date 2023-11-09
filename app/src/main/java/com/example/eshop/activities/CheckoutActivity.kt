package com.example.eshop.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eshop.R
import com.example.eshop.adapters.CartItemsListAdapter
import com.example.eshop.databinding.ActivityCheckoutBinding
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.Address
import com.example.eshop.models.CartItem
import com.example.eshop.models.Order
import com.example.eshop.models.Product
import com.example.eshop.utils.Constants
import com.example.eshop.utils.ProgressBarUtil

class CheckoutActivity : AppCompatActivity() {

    // A global variable for the selected address details.
    private var addressDetails: Address? = null

    // A global variable for the product list.
    private lateinit var productsList: ArrayList<Product>

    // A global variable for the cart list.
    private lateinit var cartItemsList: ArrayList<CartItem>

    // A global variable for the SubTotal Amount.
    private var subTotal: Double = 0.0

    // A global variable for the Total Amount.
    private var totalAmount: Double = 0.0

    // A global variable for Order details.
    private lateinit var orderDetails: Order

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
            addressDetails =
                intent.getParcelableExtra<Address>(Constants.EXTRA_SELECTED_ADDRESS)!!
        }

        if (addressDetails != null) {
            binding.tvCheckoutAddressType.text = addressDetails?.type
            binding.tvCheckoutFullName.text = addressDetails?.name
            binding.tvCheckoutAddress.text =
                "${addressDetails!!.address}, ${addressDetails!!.zipCode}"
            binding.tvCheckoutAdditionalNote.text = addressDetails?.additionalNote

            if (addressDetails?.otherDetails!!.isNotEmpty()) {
                binding.tvCheckoutOtherDetails.text = addressDetails?.otherDetails
            }
            binding.tvCheckoutMobileNumber.text = addressDetails?.mobileNumber
        }

        binding.btnPlaceOrder.setOnClickListener {
            placeAnOrder()
        }

        getProductList()
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarCheckoutActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding.toolbarCheckoutActivity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))

        FireStoreHandler().getAllProductsList(this@CheckoutActivity)
    }

    /**
     * A function to get the success result of product list.
     *
     * @param productsList
     */
    fun onSuccessProductsListFromFireStore(productsList: ArrayList<Product>) {

        this.productsList = productsList

        getCartItemsList()
    }

    /**
     * A function to get the list of cart items in the activity.
     */
    private fun getCartItemsList() {

        FireStoreHandler().getCartList(this@CheckoutActivity)
    }

    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     *
     * @param cartList
     */
    fun onSuccessCartItemsList(cartList: ArrayList<CartItem>) {

        // Hide progress dialog.
        ProgressBarUtil.hideProgressBar()

        for (product in productsList) {
            for (cart in cartList) {
                if (product.product_id == cart.product_id) {
                    cart.stock_quantity = product.stock_quantity
                }
            }
        }

        cartItemsList = cartList

        binding.rvCartListItems.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        binding.rvCartListItems.setHasFixedSize(true)

        val cartListAdapter = CartItemsListAdapter(this@CheckoutActivity, cartItemsList, false)
        binding.rvCartListItems.adapter = cartListAdapter

        // TODO Step 4: Replace the subTotal and totalAmount variables with the global variables.
        // START
        for (item in cartItemsList) {

            val availableQuantity = item.stock_quantity.toInt()

            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()

                subTotal += (price * quantity)
            }
        }

        binding.tvCheckoutSubTotal.text = "$$subTotal"
        // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
        binding.tvCheckoutShippingCharge.text = "$10.0"

        if (subTotal > 0) {
            binding.llCheckoutPlaceOrder.visibility = View.VISIBLE

            totalAmount = subTotal + 10.0
            binding.tvCheckoutTotalAmount.text = "$$totalAmount"
        } else {
            binding.llCheckoutPlaceOrder.visibility = View.GONE
        }
    }

    /**
     * A function to prepare the Order details to place an order.
     */
    private fun placeAnOrder() {

        // Show the progress dialog.
        ProgressBarUtil.showProgressBar(this, resources.getString(R.string.please_wait))

        // TODO Step 5: Now prepare the order details based on all the required details.
        orderDetails = Order(
            FireStoreHandler().getCurrentUserId(),
            cartItemsList,
            addressDetails!!,
            "My order ${System.currentTimeMillis()}",
            cartItemsList[0].image,
            subTotal.toString(),
            "10.0", // The Shipping Charge is fixed as $10 for now in our case.
            totalAmount.toString(),
        )
        FireStoreHandler().placeOrder(this@CheckoutActivity, orderDetails)
    }

    /**
     * A function to notify the success result of the order placed.
     */
    fun onOrderPlacedSuccess() {
        FireStoreHandler().updateAllDetails(this@CheckoutActivity, cartItemsList, orderDetails)
    }

    /**
     * A function to notify the success result after updating all the required details.
     */
    fun onAllDetailsUpdatedSuccessfully() {

        // Hide the progress dialog.
        ProgressBarUtil.hideProgressBar()

        Toast.makeText(this@CheckoutActivity, "Your order placed successfully.", Toast.LENGTH_SHORT)
            .show()

        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}