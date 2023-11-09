package com.example.eshop.utils

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap

object ImageExtensionIdentifier {
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}