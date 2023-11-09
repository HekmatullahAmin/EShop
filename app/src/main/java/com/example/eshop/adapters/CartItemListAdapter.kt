package com.example.eshop.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.eshop.R
import com.example.eshop.activities.CartListActivity
import com.example.eshop.firestore.FireStoreHandler
import com.example.eshop.models.CartItem
import com.example.eshop.utils.Constants
import com.example.eshop.utils.GlideImageLoader
import com.example.eshop.utils.ProgressBarUtil
import com.example.eshop.utils.SnackBarUtil

open class CartItemsListAdapter(
    private val context: Context,
    private var list: ArrayList<CartItem>,
    private val updateCartItems: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_cart_layout,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            GlideImageLoader.loadProductPicture(
                context,
                model.image,
                holder.iv_cart_item_image
            )

            holder.tv_cart_item_title.text = model.title
            holder.tv_cart_item_price.text = "$${model.price}"
            holder.tv_cart_quantity.text = model.cart_quantity

            if (model.cart_quantity == "0") {

                if (updateCartItems) {
                    holder.ib_delete_cart_item.visibility = View.VISIBLE
                } else {
                    holder.ib_delete_cart_item.visibility = View.GONE
                }
                holder.ib_remove_cart_item.visibility = View.GONE
                holder.ib_add_cart_item.visibility = View.GONE

                holder.tv_cart_quantity.text =
                    context.resources.getString(R.string.lbl_out_of_stock)

                holder.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSnackBarError
                    )
                )
            } else {

                if (updateCartItems) {
                    holder.ib_remove_cart_item.visibility = View.VISIBLE
                    holder.ib_add_cart_item.visibility = View.VISIBLE
                    holder.ib_delete_cart_item.visibility = View.VISIBLE
                } else {

                    holder.ib_remove_cart_item.visibility = View.GONE
                    holder.ib_add_cart_item.visibility = View.GONE
                    holder.ib_delete_cart_item.visibility = View.GONE
                }

                holder.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSecondaryText
                    )
                )
            }

            // TODO Step 1: Assign the click event to the ib_remove_cart_item.
            holder.ib_remove_cart_item.setOnClickListener {

                // TODO Step 6: Call the update or remove function of firestore class based on the cart quantity.
                if (model.cart_quantity == "1") {
                    FireStoreHandler().removeItemFromCart(context, model.id)
                } else {

                    val cartQuantity: Int = model.cart_quantity.toInt()

                    val itemHashMap = HashMap<String, Any>()

                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

                    // Show the progress dialog.

                    if (context is CartListActivity) {
                        ProgressBarUtil.showProgressBar(
                            context,
                            context.resources.getString(R.string.please_wait)
                        )
                    }

                    FireStoreHandler().updateMyCart(context, model.id, itemHashMap)
                }
            }

            // TODO Step 7: Assign the click event to the ib_add_cart_item.
            holder.ib_add_cart_item.setOnClickListener {

                // TODO Step 8: Call the update function of firestore class based on the cart quantity.
                val cartQuantity: Int = model.cart_quantity.toInt()

                if (cartQuantity < model.stock_quantity.toInt()) {

                    val itemHashMap = HashMap<String, Any>()

                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                    // Show the progress dialog.
                    if (context is CartListActivity) {
                        ProgressBarUtil.showProgressBar(
                            context,
                            context.resources.getString(R.string.please_wait)
                        )
                    }

                    FireStoreHandler().updateMyCart(context, model.id, itemHashMap)
                } else {
                    if (context is CartListActivity) {
                        SnackBarUtil.showSnackBar(
                            context,
                            context.findViewById(android.R.id.content),
                            context.resources.getString(
                                R.string.msg_for_available_stock,
                                model.stock_quantity
                            ),
                            true
                        )
                    }
                }
            }


            holder.ib_delete_cart_item.setOnClickListener {

                when (context) {
                    is CartListActivity -> {
                        ProgressBarUtil.showProgressBar(
                            context,
                            context.resources.getString(R.string.please_wait)
                        )
                    }
                }

                FireStoreHandler().removeItemFromCart(context, model.id)
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv_cart_item_image: ImageView = view.findViewById(R.id.iv_cart_item_image)
        val tv_cart_item_title: TextView = view.findViewById(R.id.tv_cart_item_title)
        val tv_cart_item_price: TextView = view.findViewById(R.id.tv_cart_item_price)
        val tv_cart_quantity: TextView = view.findViewById(R.id.tv_cart_quantity)
        val ib_remove_cart_item: ImageButton = view.findViewById(R.id.ib_remove_cart_item)
        val ib_add_cart_item: ImageButton = view.findViewById(R.id.ib_add_cart_item)
        val ib_delete_cart_item: ImageButton = view.findViewById(R.id.ib_delete_cart_item)
    }
}