package com.example.bikeapp.activities

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.media.MediaPlayer
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.services.SensorService
import com.example.bikeapp.services.SmsService
import kotlinx.android.synthetic.main.activity_alert_dialog.*

class AlertDialogActivity : AppCompatActivity() {

    private var waitTime = 15000L
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var myPrefs: SharedPreferences
    private lateinit var vibrater: Vibrator

    private var countDownTimer: CountDownTimer = object: CountDownTimer(waitTime, 1000){
        override fun onFinish() {
            stopMedia()
            sendAlert()
            Toast.makeText(applicationContext, "time up alert sent", Toast.LENGTH_SHORT).show()
            SensorService.alertActive = false
            finish()
        }

        override fun onTick(millisUntilFinished: Long) {
            secondsAlertDialog.text =  String.format("%d seconds", millisUntilFinished/1000)
            if(myPrefs.getBoolean(Constants.Settings.vibrate, true)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrater.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
                }else{
                    vibrater.vibrate(250)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        vibrater = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        setFinishOnTouchOutside(false)
        setContentView(R.layout.activity_alert_dialog)
        cancel.setOnClickListener {
            stopMedia()
            countDownTimer.cancel()
            SensorService.alertActive = false
//            finish()
            finishAndRemoveTask()
        }
        sendNow.setOnClickListener {
            stopMedia()
            countDownTimer.cancel()
            sendAlert()
            SensorService.alertActive = false
//            finish()
            finishAndRemoveTask()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer.isLooping = true
        startMedia()
        countDownTimer.start()
    }

    private fun sendAlert(){
        val latitude = intent.extras!!["latitude"] as Double
        val longitude = intent.extras!!["longitude"] as Double
        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude
        SmsService(this).sendAlertToAll(location)
        Toast.makeText(applicationContext, "Alert sent", Toast.LENGTH_SHORT).show()
    }

    private fun startMedia(){
        if(myPrefs.getBoolean(Constants.Settings.sound, true)){
            mediaPlayer.start()
        }

    }

    private fun stopMedia(){
        if(myPrefs.getBoolean(Constants.Settings.sound, true)){
            mediaPlayer.stop()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopMedia()
        countDownTimer.cancel()
        SensorService.alertActive = false
        finishAndRemoveTask()
    }
}
