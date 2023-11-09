package com.example.eshop.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.fragment.app.Fragment
import com.example.eshop.activities.AddEditAddressActivity
import com.example.eshop.activities.AddProductActivity
import com.example.eshop.activities.AddressListActivity
import com.example.eshop.activities.CartListActivity
import com.example.eshop.activities.CheckoutActivity
import com.example.eshop.activities.LoginActivity
import com.example.eshop.activities.ProductDetailsActivity
import com.example.eshop.activities.RegisterActivity
import com.example.eshop.activities.SettingsActivity
import com.example.eshop.activities.UserProfileActivity
import com.example.eshop.fragments.DashboardFragment
import com.example.eshop.fragments.OrdersFragment
import com.example.eshop.fragments.ProductsFragment
import com.example.eshop.fragments.SoldProductsFragment
import com.example.eshop.models.Address
import com.example.eshop.models.CartItem
import com.example.eshop.models.Order
import com.example.eshop.models.Product
import com.example.eshop.models.SoldProduct
import com.example.eshop.models.User
import com.example.eshop.utils.Constants
import com.example.eshop.utils.ImageExtensionIdentifier
import com.example.eshop.utils.ProgressBarUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FireStoreHandler {
    private val myFireStore = Firebase.firestore

    fun registerUser(activity: RegisterActivity, userInfo: User) {
        myFireStore.collection(Constants.USERS).document(userInfo.id)
            .set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.onUserRegistrationSuccess()
            }.addOnFailureListener {
                ProgressBarUtil.hideProgressBar()
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId: String = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getUserDetails(activity: Activity) {
        getCurrentUserId()?.let {
            myFireStore.collection(Constants.USERS).document(it).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)!!
                    val sharedPreferences = activity.getSharedPreferences(
                        Constants.ESHOP_PREFERENCES, Context.MODE_PRIVATE
                    )
//                    for saving our userName rather than calling it everytime
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString(
                        Constants.LOGGED_IN_USER, "${user.firstName} ${user.lastName}"
                    )
                    editor.apply()

                    when (activity) {
                        is LoginActivity -> {
                            activity.onLoggedInSuccess(user)
                        }

                        is SettingsActivity -> {
                            activity.onUserDetailsSuccess(user)
                        }
                    }
                }.addOnFailureListener {
                    when (activity) {
                        is LoginActivity -> {
                            ProgressBarUtil.hideProgressBar()
                        }

                        is SettingsActivity -> {
                            ProgressBarUtil.hideProgressBar()
                        }
                    }
                }
        }
    }

    fun updateUserDetails(activity: UserProfileActivity, userInfo: HashMap<String, Any>) {
        getCurrentUserId()?.let {
            myFireStore.collection(Constants.USERS).document(it).update(userInfo)
                .addOnSuccessListener {
                    activity.onUserProfileUpdateSuccess()
                }.addOnFailureListener {
                    when (activity) {
                        is UserProfileActivity -> {
                            ProgressBarUtil.hideProgressBar()
                        }
                    }
                }
        }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageUri: Uri?, imageType: String) {
//        get the storage reference
        val storage = Firebase.storage.reference.child(
            imageType + System.currentTimeMillis() + ImageExtensionIdentifier.getFileExtension(
                activity, imageUri
            )
        )

        //adding the file to reference
        storage.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
            // The image upload is success
            // Get the downloadable url from the task snapshot
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.onImageUploadSuccess(uri.toString())
                    }

                    is AddProductActivity -> {
                        activity.onImageUploadSuccess(uri.toString())
                    }
                }
            }

        }.addOnFailureListener {
            when (activity) {
                is UserProfileActivity -> {
                    ProgressBarUtil.hideProgressBar()
                }

                is AddProductActivity -> {
                    ProgressBarUtil.hideProgressBar()
                }
            }
        }
    }

    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {

        myFireStore.collection(Constants.PRODUCTS).document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(productInfo, SetOptions.merge()).addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.productUploadSuccess()
            }.addOnFailureListener { e ->
                ProgressBarUtil.hideProgressBar()
            }
    }

    fun getUserProductsList(fragment: Fragment) {
        // The collection name for PRODUCTS
        myFireStore.collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Product> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productsList.add(product)
                }

                when (fragment) {
                    is ProductsFragment -> {
                        fragment.onSuccessfullyReceivingProductsListFromFireStore(productsList)
                    }
                }
            }.addOnFailureListener {
                // Hide the progress dialog if there is any error based on the base class instance.
                when (fragment) {
                    is ProductsFragment -> {
                        ProgressBarUtil.hideProgressBar()
                    }
                }
            }
    }

    /**
     * A function to get all the product list from the cloud firestore.
     *
     * @param activity The activity is passed as parameter to the function because it is called from activity and need to the success result.
     */
    fun getAllProductsList(activity: Activity) {
        // The collection name for PRODUCTS
        myFireStore.collection(Constants.PRODUCTS)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Product> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productsList.add(product)
                }

                when (activity) {
                    is CartListActivity -> {
                        activity.onSuccessProductsListFromFireStore(productsList)
                    }

                    is CheckoutActivity -> {
                        activity.onSuccessProductsListFromFireStore(productsList)
                    }
                }
            }
            .addOnFailureListener {
                // Hide the progress dialog if there is any error based on the base class instance.
                when (activity) {
                    is CartListActivity -> {
                        ProgressBarUtil.hideProgressBar()
                    }

                    is CheckoutActivity -> {
                        ProgressBarUtil.hideProgressBar()
                    }
                }
            }
    }

    fun getDashboardItemLists(fragment: DashboardFragment) {
        myFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                val productList: ArrayList<Product> = ArrayList()
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)!!
                    product.product_id = i.id
                    productList.add(product)
                }
                fragment.onSuccessDashboardItemList(productList)
            }
            .addOnFailureListener {
                ProgressBarUtil.hideProgressBar()
            }
    }

    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {
        myFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document ->

                // Convert the snapshot to the object of Product data model class.
                val product = document.toObject(Product::class.java)!!

                activity.onProductDetailsSuccess(product)
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is an error.
                ProgressBarUtil.hideProgressBar()
            }
    }

    fun deleteProduct(fragment: ProductsFragment, productId: String) {

        myFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .delete()
            .addOnSuccessListener {

                // TODO Step 4: Notify the success result to the base class.
                // START
                // Notify the success result to the base class.
                fragment.onProductDeleteSuccess()
                // END
            }
            .addOnFailureListener {
                // Hide the progress dialog if there is an error.
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to add the item to the cart in the cloud firestore.
     *
     * @param activity
     * @param addToCart
     */
    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem) {

        myFireStore.collection(Constants.CART_ITEMS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addToCart, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.addToCartSuccess()
            }
            .addOnFailureListener {
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to check whether the item already exist in the cart or not.
     */
    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String) {

        myFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document ->

                // If the document size is greater than 1 it means the product is already added to the cart.
                if (document.documents.size > 0) {
                    activity.productExistsInCart()
                } else {
                    ProgressBarUtil.hideProgressBar()
                }
            }
            .addOnFailureListener {
                // Hide the progress dialog if there is an error.
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to get the cart items list from the cloud firestore.
     *
     * @param activity
     */
    fun getCartList(activity: Activity) {
        // The collection name for PRODUCTS
        myFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we have created a new instance for Cart Items ArrayList.
                val list: ArrayList<CartItem> = ArrayList()

                // A for loop as per the list of documents to convert them into Cart Items ArrayList.
                for (i in document.documents) {

                    val cartItem = i.toObject(CartItem::class.java)!!
                    cartItem.id = i.id

                    list.add(cartItem)
                }

                when (activity) {
                    is CartListActivity -> {
                        activity.onSuccessCartItemsList(list)
                    }

                    is CheckoutActivity -> {
                        activity.onSuccessCartItemsList(list)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is an error based on the activity instance.
                when (activity) {
                    is CartListActivity -> {
                        ProgressBarUtil.hideProgressBar()
                    }

                    is CheckoutActivity -> {
                        ProgressBarUtil.hideProgressBar()
                    }
                }
            }
    }

    /**
     * A function to remove the cart item from the cloud firestore.
     *
     * @param activity activity class.
     * @param cart_id cart id of the item.
     */
    fun removeItemFromCart(context: Context, cart_id: String) {

        // Cart items collection name
        myFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id) // cart id
            .delete()
            .addOnSuccessListener {

                // Notify the success result of the removed cart item from the list to the base class.
                when (context) {
                    is CartListActivity -> {
                        context.onItemRemovedSuccess()
                    }
                }
            }
            .addOnFailureListener {
                // Hide the progress dialog if there is any error.
                when (context) {
                    is CartListActivity -> {
                        ProgressBarUtil.hideProgressBar()
                    }
                }
            }
    }

    // TODO Step 2: Create a function to update the cart item in the cloud firestore.
    // START
    /**
     * A function to update the cart item in the cloud firestore.
     *
     * @param activity activity class.
     * @param id cart id of the item.
     * @param itemHashMap to be updated values.
     */
    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>) {

        // Cart items collection name
        myFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id) // cart id
            .update(itemHashMap) // A HashMap of fields which are to be updated.
            .addOnSuccessListener {

                // TODO Step 4: Notify the success result of the updated cart items list to the base class.
                // START
                // Notify the success result of the updated cart items list to the base class.
                when (context) {
                    is CartListActivity -> {
                        context.onItemUpdateSuccess()
                    }
                }
                // END
            }
            .addOnFailureListener {
                // Hide the progress dialog if there is any error.
                when (context) {
                    is CartListActivity -> {
                        ProgressBarUtil.hideProgressBar()
                    }
                }
            }
    }

    /**
     * A function to add address to the cloud firestore.
     *
     * @param activity
     * @param addressInfo
     */
    fun addAddress(activity: AddEditAddressActivity, addressInfo: Address) {

        // Collection name address.
        myFireStore.collection(Constants.ADDRESSES)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.onAddUpdateAddressSuccess()
            }
            .addOnFailureListener {
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to get the list of address from the cloud firestore.
     *
     * @param activity
     */
    fun getAddressesList(activity: AddressListActivity) {
        // The collection name for PRODUCTS
        myFireStore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                // Here we have created a new instance for address ArrayList.
                val addressList: ArrayList<Address> = ArrayList()

                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for (i in document.documents) {

                    val address = i.toObject(Address::class.java)!!
                    address.id = i.id

                    addressList.add(address)
                }

                activity.onSuccessAddressListFromFirestore(addressList)
            }
            .addOnFailureListener { e ->
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to update the existing address to the cloud firestore.
     *
     * @param activity Base class
     * @param addressInfo Which fields are to be updated.
     * @param addressId existing address id
     */
    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId: String) {

        myFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.onAddUpdateAddressSuccess()
            }
            .addOnFailureListener {
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to delete the existing address from the cloud firestore.
     *
     * @param activity Base class
     * @param addressId existing address id
     */
    fun deleteAddress(activity: AddressListActivity, addressId: String) {

        myFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.deleteAddressSuccess()
            }
            .addOnFailureListener {
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to place an order of the user in the cloud firestore.
     *
     * @param activity base class
     * @param order Order Info
     */
    fun placeOrder(activity: CheckoutActivity, order: Order) {

        myFireStore.collection(Constants.ORDERS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(order, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.onOrderPlacedSuccess()
            }
            .addOnFailureListener {
                // Hide the progress dialog if there is any error.
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to update all the required details in the cloud firestore after placing the order successfully.
     *
     * @param activity Base class.
     * @param cartList List of cart items.
     */
    fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<CartItem>, order: Order) {

        val writeBatch = myFireStore.batch()

        // Prepare the sold product details
        for (cart in cartList) {

            val soldProduct = SoldProduct(
                cart.product_owner_id,
                cart.title,
                cart.price,
                cart.cart_quantity,
                cart.image,
                order.title,
                order.order_datetime,
                order.sub_total_amount,
                order.shipping_charge,
                order.total_amount,
                order.address
            )

            val documentReference = myFireStore.collection(Constants.SOLD_PRODUCTS)
                .document()
            writeBatch.set(documentReference, soldProduct)
        }

        // Here we will update the product stock in the products collection based to cart quantity.
        for (cart in cartList) {

            val productHashMap = HashMap<String, Any>()

            productHashMap[Constants.STOCK_QUANTITY] =
                (cart.stock_quantity.toInt() - cart.cart_quantity.toInt()).toString()

            val documentReference = myFireStore.collection(Constants.PRODUCTS)
                .document(cart.product_id)

            writeBatch.update(documentReference, productHashMap)
        }

        // Delete the list of cart items
        for (cart in cartList) {

            val documentReference = myFireStore.collection(Constants.CART_ITEMS)
                .document(cart.id)
            writeBatch.delete(documentReference)
        }

        writeBatch.commit().addOnSuccessListener {

            activity.onAllDetailsUpdatedSuccessfully()

        }.addOnFailureListener {
            // Here call a function of base activity for transferring the result to it.
            ProgressBarUtil.hideProgressBar()
        }
    }

    /**
     * A function to get the list of orders from cloud firestore.
     */
    fun getMyOrdersList(fragment: OrdersFragment) {
        myFireStore.collection(Constants.ORDERS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                val list: ArrayList<Order> = ArrayList()

                for (i in document.documents) {

                    val orderItem = i.toObject(Order::class.java)!!
                    orderItem.id = i.id

                    list.add(orderItem)
                }

                fragment.populateOrdersListInUI(list)
            }
            .addOnFailureListener {
                // Here call a function of base activity for transferring the result to it.
                ProgressBarUtil.hideProgressBar()
            }
    }

    /**
     * A function to get the list of sold products from the cloud firestore.
     *
     *  @param fragment Base class
     */
    fun getSoldProductsList(fragment: SoldProductsFragment) {
        // The collection name for SOLD PRODUCTS
        myFireStore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we have created a new instance for Sold Products ArrayList.
                val list: ArrayList<SoldProduct> = ArrayList()

                // A for loop as per the list of documents to convert them into Sold Products ArrayList.
                for (i in document.documents) {

                    val soldProduct = i.toObject(SoldProduct::class.java)!!
                    soldProduct.id = i.id

                    list.add(soldProduct)
                }

                fragment.onSuccessSoldProductsList(list)
            }
            .addOnFailureListener {
                // Hide the progress dialog if there is any error.
                ProgressBarUtil.hideProgressBar()
            }
    }
}