package com.example.bikeapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bikeapp.R
import kotlinx.android.synthetic.main.activity_alert.*

class AlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        if(intent?.extras?.containsKey("msg") != null){
            alert.text = intent.getStringExtra("msg")
        }
    }
}
