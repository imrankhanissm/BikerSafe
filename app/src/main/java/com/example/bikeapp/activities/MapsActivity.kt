package com.example.bikeapp.activities

import android.Manifest
import android.accessibilityservice.GestureDescription
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
import android.util.DisplayMetrics
import android.util.Log
import com.example.bikeapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mLocationManager: LocationManager
    private var lastLocation: LatLng? = null

    private val accelerationThreshold: Float = 36F // 78
    private val gyroscopeThreshold: Float = 12F // 17
    private var accelerometerArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private lateinit var sensorManager: SensorManager
    private var maxGyroValue: Float = 0F
    private var accelerometerMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var accelerometerMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMaxArray: FloatArray = floatArrayOf(0F, 0F, 0F)
    private var gyroscopeMinArray: FloatArray = floatArrayOf(0F, 0F, 0F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(Array(1){Manifest.permission.ACCESS_FINE_LOCATION}, 1)
            requestPermissions(Array(1){Manifest.permission.ACCESS_COARSE_LOCATION}, 2)
        }
        var mLocationListener: LocationListener = object: LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String?) {}

            override fun onProviderDisabled(provider: String?) {}

            override fun onLocationChanged(location: Location?) {
                Log.d("location: ", location!!.provider.toString() + ", accuracy: " + location.accuracy.toString() + ", speed: " + location.speed.toString())
                mMap.clear()
                lastLocation = LatLng(location.latitude, location.longitude)

                var zoomLevel = 19 - (location.accuracy - 1)/2000 * 6

                mMap.addCircle(
                    CircleOptions().
                        center(lastLocation).
                        radius(location.accuracy.toDouble()).
                        strokeColor(Color.rgb(51, 138, 204)).
                        strokeWidth(2F).
                        fillColor(Color.argb(50, 51, 138, 204)).
                        zIndex(0.5F))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, zoomLevel))
            }
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.1F, mLocationListener)
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.1F, mLocationListener)



        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)

        Log.i("mytag", sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION).maximumRange.toString())
        Log.i("mytag", sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).maximumRange.toString())

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

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
//            maxGyro.text = maxGyro.toString()
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
                var intent = Intent(this, AlertActivity::class.java)
                intent.putExtra("msg", "triggered by acceleration")
                startActivity(intent)
            }
            if(gyroscopeArray[i] > gyroscopeThreshold || gyroscopeArray[i] < -gyroscopeThreshold){
                sensorManager.unregisterListener(this)
                var intent = Intent(this, AlertActivity::class.java)
                intent.putExtra("msg", "triggered by gyroscope")
                startActivity(intent)
            }
            totalAcc += accelerometerArray[i]
            totalGyro += gyroscopeArray[i]
        }
//        acceleration.text = accelerometerArray.contentToString()
//        gyroscope.text = gyroscopeArray.contentToString()
    }
}