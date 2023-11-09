package com.example.eshop.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.eshop.R
import java.io.IOException

object GlideImageLoader {
    fun loadUserPicture(context: Context, image: Any, imageView: ImageView) {
        Glide.with(context)
            .load(image)
            .centerCrop()
            .placeholder(R.drawable.user_placeholder)
            .into(imageView)
    }

    fun loadProductPicture(context: Context, image: Any, imageView: ImageView) {

        Glide
            .with(context)
            .load(image) // Uri or URL of the image
            .centerCrop() // Scale type of the image.
            .into(imageView) // the view in which the image will be loaded.
    }
}
