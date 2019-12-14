package com.example.bikeapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bikeapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private var locationPermissionCode = 1
    private var waitTime = 5000L
    private var driveMode = false

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var locationListener: LocationListener
    private lateinit var alertDialog: AlertDialog
    private var snackBar: Snackbar? = null
    private var lastLocation: Location? = null
    private var countDownTimer: CountDownTimer = object: CountDownTimer(waitTime, 1000){
                                                    override fun onFinish() {
                                                        alertDialog?.dismiss()
                                                        Toast.makeText(applicationContext, "time up alert sent", Toast.LENGTH_SHORT).show()
                                                    }

                                                    override fun onTick(millisUntilFinished: Long) {
                                                        alertDialog?.setMessage("Sending alert in " + millisUntilFinished/1000)
                                                    }
                                                }

    private val accelerationThreshold: Float = 35F // 78
    private val gyroscopeThreshold: Float = 12F // 17
    private var accelerometerArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var maxGyroValue: Float = 0F
    private var accelerometerMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var accelerometerMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("mytag", "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        alertDialog = AlertDialog.Builder(this).
                                    setTitle(title).
                                    setMessage("Sending alert in __secs")
                                    .setPositiveButton("SendNow"){ _, _ ->
                                        countDownTimer.cancel()
                                        Toast.makeText(this, "alert sent", Toast.LENGTH_SHORT).show()
                                    }
                                    .setNegativeButton("Cancel"){ _, _ ->
                                        countDownTimer.cancel()
                                        Toast.makeText(this, "alert canceled", Toast.LENGTH_SHORT).show()
                                    }
                                    .create()

        locationListener = object: LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String?) {}

            override fun onProviderDisabled(provider: String?) {}

            override fun onLocationChanged(location: Location?) {
                Log.d("debug: ", location!!.provider.toString() + ", accuracy: " + location.accuracy.toString() + ", speed: " + location.speed.toString())
                lastLocation = location
                mMap.clear()
                var locLatLng = LatLng(location.latitude, location.longitude)

                var zoomLevel = 19 - ((location.accuracy)/3000.0 * 6)
                if(location.accuracy > 100) {
                    zoomLevel -= 2
                }

                mMap.addCircle(CircleOptions().
                                center(locLatLng).
                                radius(location.accuracy.toDouble()).
                                strokeColor(Color.rgb(250, 0, 0)).
                                strokeWidth(2F).
                                fillColor(Color.argb(50, 250, 0, 0)).
                                zIndex(0.5F))
                mMap.addMarker(MarkerOptions().position(locLatLng))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, zoomLevel.toFloat()))
            }
        }

        myLocFloatingActionButton.setOnClickListener {
            toggleDriveMode()
        }

        help.setOnClickListener {
            sendAlert("Help")
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
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                requestLocationUpdates()
                registerSensorListeners()
                myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_blue_24dp)
                driveMode = true
            }else{
                snackBar = Snackbar.make(myLocFloatingActionButton, "Enable location", Snackbar.LENGTH_SHORT)
                snackBar?.show()
            }
        }
    }

    private fun driveModeOff() {
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(locationListener)
        myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_black_24dp)
        mMap.clear()
        driveMode = false
    }

    private fun registerSensorListeners(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("debug", "registered sensor listeners")
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(){
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.1F, locationListener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.1F, locationListener)
        Log.d("debug", "requested location updates")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == locationPermissionCode){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                toggleDriveMode()
            }else{
                snackBar = Snackbar.make(myLocFloatingActionButton, "Location permission required", Snackbar.LENGTH_SHORT)
                snackBar?.show()
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
//                sensorManager.unregisterListener(this)
                if(!alertDialog.isShowing){
                    sendAlert("Collision Detected")
                }
            }
            if(gyroscopeArray[i] > gyroscopeThreshold || gyroscopeArray[i] < -gyroscopeThreshold){
//                sensorManager.unregisterListener(this)
                if(!alertDialog.isShowing){
                    sendAlert("Collision Detected")
                }
            }
            totalAcc += accelerometerArray[i]
            totalGyro += gyroscopeArray[i]
        }
    }

    private fun sendAlert(title: String){
        countDownTimer.start()

//        alertDialog = AlertDialog.Builder(this).
//            setTitle(title).
//            setMessage("Sending alert in __secs")
//            .setPositiveButton("SendNow"){ _, _ ->
//                countDownTimer.cancel()
//                Toast.makeText(this, "alert sent", Toast.LENGTH_SHORT).show()
//            }
//            .setNegativeButton("Cancel"){ _, _ ->
//                countDownTimer.cancel()
//                Toast.makeText(this, "alert canceled", Toast.LENGTH_SHORT).show()
//            }
//            .create()

        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
    }
}