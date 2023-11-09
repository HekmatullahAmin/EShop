package com.example.eshop.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.eshop.R
import com.example.eshop.utils.Constants

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userName: TextView = findViewById(R.id.userNameTV)
        val sharedPreferences =
            getSharedPreferences(Constants.ESHOP_PREFERENCES, Context.MODE_PRIVATE)
        userName.text = "Hi " + sharedPreferences.getString(Constants.LOGGED_IN_USER, "Guest")
    }
}