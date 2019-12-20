package com.example.bikeapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import kotlinx.android.synthetic.main.activity_main.*

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myPrefs: SharedPreferences = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        setContentView(R.layout.activity_main)
        myPrefs.edit().putBoolean("notFirst", true).apply()
        hello.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
