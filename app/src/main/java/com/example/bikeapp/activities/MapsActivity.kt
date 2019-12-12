package com.example.bikeapp.activities

import android.Manifest
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.bikeapp.R
import com.example.bikeapp.services.MyIntentService

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mLocationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var mLocationListener: LocationListener
    private var snackBar: Snackbar? = null
    private var lastLocation: Location? = null

    private val accelerationThreshold: Float = 36F // 78
    private val gyroscopeThreshold: Float = 12F // 17
    private var accelerometerArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var maxGyroValue: Float = 0F
    private var accelerometerMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var accelerometerMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)

    private var locationPermissionCode = 1
    private var waitTime = 5000L
    private var driveMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("mytag", "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mLocationListener = object: LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String?) {}

            override fun onProviderDisabled(provider: String?) {}

            override fun onLocationChanged(location: Location?) {
                Log.d("location: ", location!!.provider.toString() + ", accuracy: " + location.accuracy.toString() + ", speed: " + location.speed.toString())
                mMap.clear()
                var locLatLng = LatLng(location.latitude, location.longitude)
                lastLocation = location

                var zoomLevel = 19 - ((location.accuracy)/3000.0 * 6)
                if(location.accuracy > 100) {
                    zoomLevel -= 2
                }

                mMap.addCircle(
                    CircleOptions().
                        center(locLatLng).
                        radius(location.accuracy.toDouble()).
                        strokeColor(Color.rgb(51, 138, 204)).
                        strokeWidth(2F).
                        fillColor(Color.argb(50, 51, 138, 204)).
                        zIndex(0.5F))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, zoomLevel.toFloat()))
            }
        }
        registerSensorListeners()

        myLocFloatingActionButton.setOnClickListener {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(Array(1){Manifest.permission.ACCESS_FINE_LOCATION}, locationPermissionCode)
            }else{
                if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    toggleDriveMode()
                    snackBar?.dismiss()
                }else{
                    snackBar?.dismiss()
                    snackBar = Snackbar.make(myLocFloatingActionButton, "Enable location", Snackbar.LENGTH_INDEFINITE)
                    snackBar?.show()
                }
            }
        }

        help.setOnClickListener {
            sendAlert()
        }

//        var intent = Intent(this, MyIntentService::class.java)
////        ContextCompat.startForegroundService(this, intent)
    }

    private fun toggleDriveMode(){
        if(driveMode){
            driveModeOff()
        }else{
            driveModeOn()
        }
    }

    private fun driveModeOn() {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(Array(1){Manifest.permission.ACCESS_FINE_LOCATION}, locationPermissionCode)
        }else{
            if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                requestLocationUpdates()
                registerSensorListeners()
                myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_blue_24dp)
                driveMode = true
            }else{
                snackBar = Snackbar.make(myLocFloatingActionButton, "Enable location", Snackbar.LENGTH_INDEFINITE)
                snackBar?.show()
            }
        }
    }

    private fun driveModeOff() {
        sensorManager.unregisterListener(this)
        mLocationManager.removeUpdates(mLocationListener)
        myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_black_24dp)
        mMap.clear()
        driveMode = false
    }

    private fun registerSensorListeners(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(){
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.1F, mLocationListener)
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.1F, mLocationListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == locationPermissionCode){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                toggleDriveMode()
            }else{
                Toast.makeText(this, "location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) {
        var x: Float = event!!.values[0]
        var y: Float = event.values[1]
        var z: Float = event.values[2]
        if(event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
            accelerometerArray = floatArrayOf(x, y, z)
            for(i in 0..2){
                accelerometerMaxArray[i] = Math.max(accelerometerMaxArray[i], x)
                accelerometerMaxArray[i] = Math.max(accelerometerMaxArray[i], y)
                accelerometerMaxArray[i] = Math.max(accelerometerMaxArray[i], z)

                accelerometerMinArray[i] = Math.min(accelerometerMinArray[i], x)
                accelerometerMinArray[i] = Math.min(accelerometerMinArray[i], y)
                accelerometerMinArray[i] = Math.min(accelerometerMinArray[i], z)
            }
            maxGyroValue = Math.max(maxGyroValue, Math.max(x, Math.max(y, z)))
        }else if(event.sensor.type == Sensor.TYPE_GYROSCOPE){
            gyroscopeArray = floatArrayOf(x, y, z)
            for(i in 0..2){
                gyroscopeMaxArray[i] = Math.max(gyroscopeMaxArray[i], x)
                gyroscopeMaxArray[i] = Math.max(gyroscopeMaxArray[i], y)
                gyroscopeMaxArray[i] = Math.max(gyroscopeMaxArray[i], z)

                gyroscopeMinArray[i] = Math.min(gyroscopeMinArray[i], x)
                gyroscopeMinArray[i] = Math.min(gyroscopeMinArray[i], y)
                gyroscopeMinArray[i] = Math.min(gyroscopeMinArray[i], z)
            }
        }
        var totalAcc = 0F
        var totalGyro = 0F
        for(i in 0..2){
            if(accelerometerArray[i] > accelerationThreshold || accelerometerArray[i] < -accelerationThreshold){
                sensorManager.unregisterListener(this)
                sendAlert()
            }
            if(gyroscopeArray[i] > gyroscopeThreshold || gyroscopeArray[i] < -gyroscopeThreshold){
                sensorManager.unregisterListener(this)
                sendAlert()
            }
            totalAcc += accelerometerArray[i]
            totalGyro += gyroscopeArray[i]
        }
    }

    private fun sendAlert(){
        var alertDialog: AlertDialog? = null
        var countDownTimer = object: CountDownTimer(waitTime, 1000){
            override fun onFinish() {
                alertDialog?.dismiss()
                Toast.makeText(applicationContext, "time up alert sent", Toast.LENGTH_SHORT).show()
            }

            override fun onTick(millisUntilFinished: Long) {
                Toast.makeText(applicationContext, (millisUntilFinished/1000).toString(), Toast.LENGTH_SHORT).show()
            }
        }.start()

        alertDialog = AlertDialog.Builder(this).
            setTitle("Collision detected").
            setMessage("Sending alert in __secs")
            .setPositiveButton("SendNow"){ dialog, which ->
                countDownTimer.cancel()
                Toast.makeText(this, "alert sent", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel"){dialog, which ->
                countDownTimer.cancel()
                Toast.makeText(this, "alert canceled", Toast.LENGTH_SHORT).show()
            }
            .create()

        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    override fun onPause() {
        super.onPause()
//        sensorManager.unregisterListener(this)
//        mLocationManager.removeUpdates(mLocationListener)
    }

    override fun onResume() {
        super.onResume()
//        registerSensorListeners()
//        requestLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}