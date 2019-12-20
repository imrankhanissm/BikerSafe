package com.example.bikeapp.activities

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.models.User
import com.example.bikeapp.services.MyIntentService

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*
import kotlin.math.max
import kotlin.math.min

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private var locationPermissionCode = 1
    private var waitTime = 2000L
    private var driveMode = false

    private lateinit var mMap: GoogleMap
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var alertDialog: AlertDialog
    private var snackBar: Snackbar? = null
    private var lastLocation: Location? = null
    private var countDownTimer: CountDownTimer = object: CountDownTimer(waitTime, 1000){
                                                    override fun onFinish() {
                                                        alertDialog.dismiss()
                                                        Toast.makeText(applicationContext, "time up alert sent", Toast.LENGTH_SHORT).show()
                                                    }

                                                    override fun onTick(millisUntilFinished: Long) {
                                                        alertDialog.setMessage("Sending alert in " + millisUntilFinished/1000)
                                                    }
                                                }

    private val accelerationThreshold: Float = 12F // 78
    private val gyroscopeThreshold: Float = 12F // 17
    private var accelerometerArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var maxGyroValue: Float = 0F
    private var accelerometerMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var accelerometerMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        if(!myPrefs.contains("notFirst")) {
            startActivity(Intent(this, SplashScreenActivity::class.java))
            finish()
            return
        }else if(!myPrefs.contains(User.name)){
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }

        supportActionBar?.title = "Biker App"
        Log.d("mytag", "oncreate")
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        alertDialog = AlertDialog.Builder(this).
                                    setTitle("").
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
//        alertDialog.setCancelable(true)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object: LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String?) {}

            override fun onProviderDisabled(provider: String?) {}

            override fun onLocationChanged(location: Location?) {
                Log.d("debug: ", location!!.provider.toString() + ", accuracy: " + location.accuracy.toString() + ", speed: " + location.speed.toString())
                lastLocation = location
                mMap.clear()
                val locLatLng = LatLng(location.latitude, location.longitude)

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

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.maps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.mapsMenuProfile){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }else if(item?.itemId == R.id.mapsMenuSettings){
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT)
        }
        return super.onOptionsItemSelected(item)
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
                // start service
                val intent = Intent(this, MyIntentService::class.java)
                ContextCompat.startForegroundService(this, intent)
            }else{
                snackBar = Snackbar.make(myLocFloatingActionButton, "Enable location", Snackbar.LENGTH_SHORT)
                snackBar?.show()
            }
        }
    }

    private fun driveModeOff() {
        unregisterSensorListeners()
        removeLocationUpdates()
        myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_black_24dp)
        mMap.clear()
        driveMode = false
        val intent = Intent(this, MyIntentService::class.java)
        stopService(intent)
    }

    private fun registerSensorListeners(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("debug", "registered sensor listeners")
    }

    private fun unregisterSensorListeners(){
        sensorManager.unregisterListener(this)
    }

    private fun removeLocationUpdates(){
        locationManager.removeUpdates(locationListener)
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
        Log.d("debug", "collision detected")
        countDownTimer.start()
        alertDialog.setTitle(title)
        alertDialog.show()
    }

//    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
//        mMap.isMyLocationEnabled = true
    }
}