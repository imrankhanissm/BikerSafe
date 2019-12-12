package com.example.bikeapp.services

import android.annotation.TargetApi
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.example.bikeapp.R

class MyIntentService : IntentService("MyIntentService") {

    @TargetApi(Build.VERSION_CODES.O)
    override fun onHandleIntent(intent: Intent?) {
        var notChannel = NotificationChannel("ch1", "channel1", NotificationManager.IMPORTANCE_DEFAULT)
        var notMng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var not = Notification.Builder(this).setContentTitle("myNot")
            .setContentText("myNotText")
            .setSmallIcon(R.drawable.ic_my_location_blue_24dp)
            .setChannelId("ch1")
            .build()
        notMng.createNotificationChannel(notChannel)
        notMng.notify(1, not)

        startForeground(1, not)

        for (i in 1..10){
            SystemClock.sleep(1000)
            Log.d("debug", "intentService: $i")
        }

        Log.d("debug", "onHandleIntent")
        Toast.makeText(this, "onHangleIntent", Toast.LENGTH_SHORT).show()
    }
}
