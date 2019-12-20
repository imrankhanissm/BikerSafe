package com.example.bikeapp.services

import android.annotation.TargetApi
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.bikeapp.R

class MyIntentService : IntentService("MyIntentService"){

    @TargetApi(Build.VERSION_CODES.O)
    override fun onHandleIntent(intent: Intent?) {
        var channelId = "ch1"
        var notChannel = NotificationChannel(channelId, "channel1", NotificationManager.IMPORTANCE_DEFAULT)
        var notMng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var not = NotificationCompat.Builder(this, channelId).setContentTitle("Biker App")
            .setContentText("Drive Mode On")
            .setSmallIcon(R.drawable.ic_directions_bike_blue_24dp)
            .setChannelId(channelId)
            .build()
        notMng.createNotificationChannel(notChannel)
        notMng.notify(1, not)

        startForeground(1, not)

        for (i in 1..20){
            SystemClock.sleep(1000)
            Log.d("debug", "intentService: $i")
        }

        Log.d("debug", "onHandleIntent")
        Toast.makeText(this, "onHandleIntent", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        Log.d("debug", "service destroyed")
        super.onDestroy()
    }
}
