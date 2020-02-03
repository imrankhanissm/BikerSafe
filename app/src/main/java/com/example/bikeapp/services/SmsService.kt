package com.example.bikeapp.services

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.Contact

class SmsService(private val context: Context) {
    private var message = "Test Accident alert\n"
    private var mapsLink = "https://www.google.com/maps/search/?api=1&query="
    private var smsManager: SmsManager = SmsManager.getDefault()
    private var dbHelper: DBHelper = DBHelper(context)

    val phoneStateListener = object: PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            Toast.makeText(context, "$state, $phoneNumber", Toast.LENGTH_SHORT).show()
        }

    }
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun sendAlertToAll(location: Location, call: Boolean){
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        val contacts = dbHelper.getContacts()
        if (contacts != null) {
            for(i in contacts){
                if(i.isValid()){
                    sendSms(i, location)
                }
                if(call) {
                    callPhone(i)
                }
            }
        }
    }

    private fun callPhone(i: Contact) {
        val phonecallIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${i.countryCode}${i.phoneNo}"))
        ContextCompat.startActivity(context, phonecallIntent, null)
    }

    private fun sendSms(contact: Contact, location: Location){
        val msg = message + " " + mapsLink + location.latitude + "," + location.longitude
        smsManager.sendTextMessage(contact.countryCode + contact.phoneNo, null, msg, null, null)
    }
}
