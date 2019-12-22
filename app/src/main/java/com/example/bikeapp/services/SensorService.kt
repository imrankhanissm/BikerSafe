package com.example.bikeapp.services

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.activities.AlertDialogActivity
import com.google.android.gms.location.*
import kotlin.math.max
import kotlin.math.min

class SensorService : Service(), SensorEventListener {

    companion object{
        var alertActive = false
        var serviceActive = false
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null

    private val accelerationThreshold: Float = 12F // 78
    private val gyroscopeThreshold: Float = 12F // 17
    private var accelerometerArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var maxGyroValue: Float = 0F
    private var accelerometerMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var accelerometerMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {

        Log.d("debug", "sensor service oncreate")
        serviceActive = true
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                lastLocation = p0?.lastLocation
                Log.d("debug", "location from fused " + p0?.lastLocation?.latitude.toString() + ", " + p0?.lastLocation?.longitude.toString() + ", " + p0?.lastLocation?.accuracy)
                sendLocationToUI()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "ch1"
        val notChannel = NotificationChannel(channelId, "channel1", NotificationManager.IMPORTANCE_DEFAULT)
        val notMng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val not = NotificationCompat.Builder(this, channelId).setContentTitle("Biker App")
            .setContentText("Drive Mode On")
            .setSmallIcon(R.drawable.ic_directions_bike_blue_24dp)
            .setChannelId(channelId)
            .build()
        notMng.createNotificationChannel(notChannel)
        notMng.notify(1, not)

        startForeground(1, not)

        requestLocationUpdates()
        registerSensorListeners()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("debug", "sensor service ondestroy")
        unregisterSensorListeners()
        removeLocationUpdates()
        serviceActive = false
        super.onDestroy()
    }

    private fun registerSensorListeners(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("debug", "registered sensor listeners")
    }

    private fun unregisterSensorListeners(){
        sensorManager.unregisterListener(this)
        Log.d("debug", "unregistered sensor listeners")
    }

    private fun requestLocationUpdates(){
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000

        locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun removeLocationUpdates(){
        locationProvider.removeLocationUpdates(locationCallback)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) {
        val x: Float = event!!.values[0]
        val y: Float = event.values[1]
        val z: Float = event.values[2]
        if(event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
            accelerometerArray = floatArrayOf(x, y, z)
            for(i in 0..2){
                accelerometerMaxArray[i] = max(accelerometerMaxArray[i], x)
                accelerometerMaxArray[i] = max(accelerometerMaxArray[i], y)
                accelerometerMaxArray[i] = max(accelerometerMaxArray[i], z)

                accelerometerMinArray[i] = min(accelerometerMinArray[i], x)
                accelerometerMinArray[i] = min(accelerometerMinArray[i], y)
                accelerometerMinArray[i] = min(accelerometerMinArray[i], z)
            }
            maxGyroValue = max(maxGyroValue, max(x, max(y, z)))
        }else if(event.sensor.type == Sensor.TYPE_GYROSCOPE){
            gyroscopeArray = floatArrayOf(x, y, z)
            for(i in 0..2){
                gyroscopeMaxArray[i] = max(gyroscopeMaxArray[i], x)
                gyroscopeMaxArray[i] = max(gyroscopeMaxArray[i], y)
                gyroscopeMaxArray[i] = max(gyroscopeMaxArray[i], z)

                gyroscopeMinArray[i] = min(gyroscopeMinArray[i], x)
                gyroscopeMinArray[i] = min(gyroscopeMinArray[i], y)
                gyroscopeMinArray[i] = min(gyroscopeMinArray[i], z)
            }
        }
        var totalAcc = 0F
        var totalGyro = 0F
        for(i in 0..2){
            if(accelerometerArray[i] > accelerationThreshold || accelerometerArray[i] < -accelerationThreshold){
//                sensorManager.unregisterListener(this)
                showAlert()
            }
            if(gyroscopeArray[i] > gyroscopeThreshold || gyroscopeArray[i] < -gyroscopeThreshold){
//                sensorManager.unregisterListener(this)
                showAlert()
            }
            totalAcc += accelerometerArray[i]
            totalGyro += gyroscopeArray[i]
        }
    }

    private fun showAlert(){
        if(alertActive){
           return
        }
        alertActive = true
        Log.d("debug", "Collision detected")
        val intent = Intent(this, AlertDialogActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        startActivity(intent)
    }

    private fun sendLocationToUI(){
        val intent = Intent(Constants.locationFromService)
        intent.putExtra("latitude", lastLocation?.latitude)
        intent.putExtra("longitude", lastLocation?.longitude)
        intent.putExtra("accuracy", lastLocation?.accuracy)
        localBroadcastManager.sendBroadcast(intent)
    }
}
