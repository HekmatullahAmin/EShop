package com.example.eshop.utils

import android.app.Dialog
import android.content.Context
import com.example.eshop.R
import com.example.eshop.widgets.MyBoldTextView

object ProgressBarUtil {
    private var progressBar: Dialog? = null

    fun showProgressBar(context: Context, message: String = context.resources.getString(R.string.please_wait)) {
        progressBar = Dialog(context)
        progressBar?.setContentView(R.layout.custom_progress_bar)
        val progressBarTV = progressBar?.findViewById<MyBoldTextView>(R.id.tv_progress_text)
        progressBarTV?.text = message
        progressBar?.setCancelable(false)
        progressBar?.setCanceledOnTouchOutside(false)
        progressBar?.show()
    }

    fun hideProgressBar() {
        progressBar?.dismiss()
    }
}