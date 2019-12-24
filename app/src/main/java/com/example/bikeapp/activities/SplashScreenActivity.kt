package com.example.bikeapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        setContentView(R.layout.activity_splash_screen)
        super.onCreate(savedInstanceState)
//        val myPrefs: SharedPreferences = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
//        myPrefs.edit().putBoolean("notFirst", true).apply()
        startActivity(Intent(this, MapsActivity::class.java))
        finish()
    }
}
