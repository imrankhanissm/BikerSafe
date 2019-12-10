package com.example.bikeapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    lateinit var myPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        if(myPrefs.contains("username")){
            startActivity(Intent(this, MapsActivity::class.java))
            finish()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        register.setOnClickListener{
            myPrefs.edit().putString("username", "imran").apply()
            startActivity(Intent(this, MapsActivity::class.java))
            finish()
        }
    }
}
