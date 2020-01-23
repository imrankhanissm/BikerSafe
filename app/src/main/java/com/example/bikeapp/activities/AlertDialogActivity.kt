package com.example.bikeapp.activities

import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.R
import com.example.bikeapp.services.SensorService
import com.example.bikeapp.services.SmsService
import kotlinx.android.synthetic.main.activity_alert_dialog.*

class AlertDialogActivity : AppCompatActivity() {

    private var waitTime = 15000L
    private var countDownTimer: CountDownTimer = object: CountDownTimer(waitTime, 1000){
        override fun onFinish() {
            mediaPlayer.stop()
            sendAlert()
            Toast.makeText(applicationContext, "time up alert sent", Toast.LENGTH_SHORT).show()
            SensorService.alertActive = false
            finish()
        }

        override fun onTick(millisUntilFinished: Long) {
            secondsAlertDialog.text = (millisUntilFinished/1000).toString() + " seconds"
        }
    }
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
        setContentView(R.layout.activity_alert_dialog)
        cancel.setOnClickListener {
            mediaPlayer.stop()
            countDownTimer.cancel()
            SensorService.alertActive = false
//            finish()
            finishAndRemoveTask()
        }
        sendNow.setOnClickListener {
            mediaPlayer.stop()
            countDownTimer.cancel()
            sendAlert()
            SensorService.alertActive = false
//            finish()
            finishAndRemoveTask()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        countDownTimer.start()
    }

    private fun sendAlert(){
        var dest = "+918341483786"
        var msg = "alert test"
        var latitude = intent.extras!!["latitude"] as Double
        var longitude = intent.extras!!["longitude"] as Double
        var location = Location("")
        location.latitude = latitude
        location.longitude = longitude
        SmsService(this).sendAlertToAll(location)
        Toast.makeText(applicationContext, "Alert sent", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        countDownTimer.cancel()
        SensorService.alertActive = false
        finishAndRemoveTask()
    }
}
