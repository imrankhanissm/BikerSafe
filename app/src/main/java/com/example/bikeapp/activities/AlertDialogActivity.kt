package com.example.bikeapp.activities

import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.R
import com.example.bikeapp.services.SensorService
import com.example.bikeapp.services.SmsService
import kotlinx.android.synthetic.main.activity_alert_dialog.*

class AlertDialogActivity : AppCompatActivity() {

    private var waitTime = 5000L
    private var countDownTimer: CountDownTimer = object: CountDownTimer(waitTime, 1000){
        override fun onFinish() {
            sendAlert()
            Toast.makeText(applicationContext, "time up alert sent", Toast.LENGTH_SHORT).show()
            SensorService.alertActive = false
            finish()
        }

        override fun onTick(millisUntilFinished: Long) {
            secondsAlertDialog.text = (millisUntilFinished/1000).toString() + "seconds"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_dialog)
        cancel.setOnClickListener {
            countDownTimer.cancel()
            SensorService.alertActive = false
            finish()
        }
        sendNow.setOnClickListener {
            countDownTimer.cancel()
            sendAlert()
            SensorService.alertActive = false
            finish()
        }
        countDownTimer.start()
    }

    private fun sendAlert(){
        var dest = "+918341483786"
        var msg = "alert test"
//        SmsService.sendSms(dest, msg)
        Toast.makeText(applicationContext, "Alert sent", Toast.LENGTH_SHORT).show()

    }
}
