package com.example.bikeapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.media.MediaPlayer
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.services.SensorService
import com.example.bikeapp.services.SmsService
import kotlinx.android.synthetic.main.activity_alert_dialog.*

class AlertDialogActivity : AppCompatActivity() {
    private var waitTime = 15000
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var myPrefs: SharedPreferences
    private lateinit var vibrator: Vibrator
    private var vibrate: Boolean = true
    private lateinit var localBroadcastManager: LocalBroadcastManager


    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!SensorService.alertActive){
            val mapsIntent = Intent(this, MapsActivity::class.java)
            startActivity(mapsIntent)
            finish()
            return
        }

        myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        waitTime = myPrefs.getInt(Constants.Settings.countDownTime, Constants.Settings.countDownTimeDefault)*1000
        vibrate = myPrefs.getBoolean(Constants.Settings.vibrate, true)

        countDownTimer = object: CountDownTimer(waitTime.toLong(), 1000){
            override fun onFinish() {
                stopMedia()
                sendAlert()
                Toast.makeText(applicationContext, "time up alert sent", Toast.LENGTH_SHORT).show()
                SensorService.alertActive = false
                finishAndRemoveTask()
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsAlertDialog.text =  String.format("%d seconds", millisUntilFinished/1000)
                if(vibrate){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
                    }else{
                        vibrator.vibrate(250)
                    }
                }
            }
        }

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
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    private fun sendAlert(){
//        val latitude = intent.extras!!["latitude"] as Double
//        val longitude = intent.extras!!["longitude"] as Double
//        val location = Location("")
//        location.latitude = latitude
//        location.longitude = longitude
//        SmsService(this).sendAlertToAll(location, myPrefs.getBoolean("call", false))
//        Toast.makeText(applicationContext, "Alert sent", Toast.LENGTH_SHORT).show()



        val intent = Intent(Constants.callContacts)
        localBroadcastManager.sendBroadcast(intent)
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
        stopMedia()
        countDownTimer.cancel()
        SensorService.alertActive = false
        finishAndRemoveTask()
        super.onBackPressed()
    }
}
