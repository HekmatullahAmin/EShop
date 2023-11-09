package com.example.eshop.utils

object Constants {

    // Firebase Constants
    // This is used for the collection name for USERS.
    const val USERS = "users"
    const val PRODUCTS = "products"
    const val CART_ITEMS: String = "cart_items"
    const val ORDERS: String = "orders"
    const val SOLD_PRODUCTS: String = "sold_products"

    const val ESHOP_PREFERENCES = "eShopPreferences"
    const val LOGGED_IN_USER = "loggedInUser"

    // Intent extra constants.
    const val USER_DETAILS = "userDetails"
    const val EXTRA_PRODUCT_ID = "extra_product_id"
    const val EXTRA_PRODUCT_OWNER_ID: String = "extra_product_owner_id"
    const val EXTRA_ADDRESS_DETAILS: String = "AddressDetails"
    const val EXTRA_MY_ORDER_DETAILS: String = "extra_my_order_details"
    const val EXTRA_SOLD_PRODUCT_DETAILS: String = "extra_sold_product_details"

    const val USER_PROFILE_IMAGE = "User_Profile_Image"

    const val PRODUCT_IMAGE: String = "Product_Image"

    const val USER_ID: String = "user_id"
    const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1

    const val PRODUCT_ID: String = "product_id"
    const val DEFAULT_CART_QUANTITY: String = "1"
    const val CART_QUANTITY: String = "cart_quantity"
    const val STOCK_QUANTITY: String = "stock_quantity"

    const val HOME: String = "Home"
    const val OFFICE: String = "Office"
    const val OTHER: String = "Other"

    // TODO Step 1: Declare a constant to pass the value through intent in the address listing screen which will
    //  help to select the address to proceed with checkout.
    const val EXTRA_SELECT_ADDRESS: String = "extra_select_address"

    // TODO Step 11: Declare a global constant variable to notify the add address.
    const val ADD_ADDRESS_REQUEST_CODE: Int = 121
    const val ADDRESSES: String = "addresses"
    const val EXTRA_SELECTED_ADDRESS: String = "extra_selected_address"
}