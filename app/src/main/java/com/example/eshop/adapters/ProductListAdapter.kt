package com.example.eshop.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eshop.R
import com.example.eshop.activities.ProductDetailsActivity
import com.example.eshop.fragments.ProductsFragment
import com.example.eshop.models.Product
import com.example.eshop.utils.Constants
import com.example.eshop.utils.GlideImageLoader
import com.example.eshop.widgets.MyBoldTextView

open class ProductListAdapter(
    private val context: Context,
    private var list: ArrayList<Product>,
    private val fragment: ProductsFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.product_list_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            GlideImageLoader.loadProductPicture(context, model.image, holder.itemImage)

            holder.itemName.text = model.title
            holder.itemPrice.text = "$${model.price}"


            holder.itemDelete.setOnClickListener {
                fragment.deleteProduct(model.product_id)
            }

            holder.itemView.setOnClickListener {
                // Launch Product details screen.
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, model.product_id)
                intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, model.user_id)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.iv_item_image)
        val itemName: MyBoldTextView = view.findViewById(R.id.tv_item_name)
        val itemPrice: TextView = view.findViewById(R.id.tv_item_price)
        val itemDelete: ImageButton = view.findViewById(R.id.ib_delete_product)
    }
}