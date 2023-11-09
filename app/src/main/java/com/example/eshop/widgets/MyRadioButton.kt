package com.example.eshop.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton

class MyRadioButton(context: Context, attributeSet: AttributeSet) :
    AppCompatRadioButton(context, attributeSet) {
    init {
        applyFont()
    }

    private fun applyFont() {
        val typFace: Typeface = Typeface.createFromAsset(context.assets, "Montserrat-Bold.ttf")
        typeface = typFace
    }
}