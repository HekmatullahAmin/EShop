package com.example.eshop.utils

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.example.eshop.R
import com.google.android.material.snackbar.Snackbar

object SnackBarUtil {
    fun showSnackBar(
        context: Context,
        view: View,
        message: String,
        isErrorMessage: Boolean,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val snackBar = Snackbar.make(view, message, duration)
        val snackBarView = snackBar.view
        if (isErrorMessage) {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.colorSnackBarError)
            )
        } else {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.colorSnackBarSuccess)
            )
        }
        snackBar.show()
    }
}