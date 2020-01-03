package com.example.bikeapp.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.models.User
import com.example.bikeapp.services.SensorService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val locationPermissionCode = 1
    private val sendSmsPermissionCode = 2

    private lateinit var mMap: GoogleMap
    private lateinit var receiver: BroadcastReceiver
    private lateinit var locationManager: LocationManager
    private lateinit var alertDialog: AlertDialog
    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("lifecycle", "onCreate")
        val myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        if(!myPrefs.contains(User.name)){
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }

        supportActionBar?.title = "Biker App"
        Log.d("debug", "oncreate")
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        alertDialog = AlertDialog.Builder(this).
                                    setTitle("Help").
                                    setMessage("Send Alert")
                                    .setPositiveButton("Send"){ _, _ ->
                                        sendAlert()
                                        alertDialog.dismiss()
                                        Toast.makeText(this, "alert sent", Toast.LENGTH_SHORT).show()
                                    }
                                    .setNegativeButton("Cancel"){ _, _ ->
                                        alertDialog.dismiss()
                                        Toast.makeText(this, "alert canceled", Toast.LENGTH_SHORT).show()
                                    }
                                    .create()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("intent", intent?.action)
                if(intent?.action == Constants.serviceStopped){
//                    myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_gray_24dp)
                    toggleFloatingActionButton(false)
                    mMap.clear()
                }else{
                    val latitude = intent?.extras?.get("latitude") as Double
                    val longitude = intent.extras?.get("longitude") as Double
                    val accuracy = intent.extras?.get("accuracy") as Float
                    setMarkerAndCircle(latitude, longitude, accuracy)
                }
            }
        }

        myLocFloatingActionButton.setOnClickListener {
            toggleDriveMode()
        }

        help.setOnClickListener {
            alertDialog.show()
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(Constants.locationFromService))
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter((Constants.serviceStopped)))
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE), locationPermissionCode)
    }

    override fun onResume() {
        super.onResume()
        Log.d("lifecycle", "onResume")
        if(SensorService.serviceActive){
            toggleFloatingActionButton(true)
        }else{
            toggleFloatingActionButton(false)
        }
    }

    override fun onDestroy() {
        if(::receiver.isInitialized){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.maps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.mapsMenuSettings){
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleDriveMode(){
        if(SensorService.serviceActive){
            driveModeOff()
        }else{
            driveModeOn()
        }
    }

    private fun driveModeOn() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE), locationPermissionCode)
        }else{
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_color_primary_24dp)
                toggleFloatingActionButton(true)
                // start service
                val intent = Intent(this, SensorService::class.java)
                ContextCompat.startForegroundService(this, intent)
            }else{
                snackBar = Snackbar.make(myLocFloatingActionButton, "Enable location", Snackbar.LENGTH_SHORT)
                snackBar?.show()
            }
        }
    }

    private fun driveModeOff() {
//        myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_gray_24dp)
        toggleFloatingActionButton(false)
        mMap.clear()
        val intent = Intent(this, SensorService::class.java)
        stopService(intent)
    }

    private fun toggleFloatingActionButton(switch: Boolean){
        if(switch){
            myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_white_24dp)
            myLocFloatingActionButton.backgroundTintList = ColorStateList.valueOf(getColor(R.color.colorPrimary))
        }else{
            myLocFloatingActionButton.setImageResource(R.drawable.ic_directions_bike_gray_24dp)
            myLocFloatingActionButton.backgroundTintList = ColorStateList.valueOf(getColor(R.color.white))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if(requestCode == locationPermissionCode){
//            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//                snackBar = Snackbar.make(myLocFloatingActionButton, "Location permission required", Snackbar.LENGTH_SHORT)
//                snackBar?.show()
//            }
//        }
    }

    private fun sendAlert(){
//        SmsService(this).sendAlertToAll()
        Toast.makeText(this, "Alert sent from activity", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
//        mMap.isMyLocationEnabled = true
    }

    private fun setMarkerAndCircle(latitude: Double, longitude: Double, accuracy: Float){
        mMap.clear()
        val locLatLng = LatLng(latitude, longitude)

        var zoomLevel = 19 - (accuracy/3000.0 * 6)
        if(accuracy > 100) {
            zoomLevel -= 2
        }

        mMap.addCircle(CircleOptions().
            center(locLatLng).
            radius(accuracy.toDouble()).
            strokeColor(getColor(R.color.colorPrimary)).
            strokeWidth(2F).
            fillColor(Color.argb(50, Color.red(getColor(R.color.colorPrimary)), Color.green(getColor(R.color.colorPrimary)), Color.blue(getColor(R.color.colorPrimary)))))
        mMap.addMarker(MarkerOptions().position(locLatLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, zoomLevel.toFloat()))
    }
}