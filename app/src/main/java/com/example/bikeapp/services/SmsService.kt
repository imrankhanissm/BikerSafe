package com.example.bikeapp.services

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.Contact

class SmsService(private val context: Context) {
    private var message = "Test Accident alert\n"
    private var mapsLink = "https://www.google.com/maps/search/?api=1&query="
    private var smsManager: SmsManager = SmsManager.getDefault()
    private var dbHelper: DBHelper = DBHelper(context)
    private var contList = mutableListOf<Contact>()

    private val phoneStateListener = object: PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if(state == TelephonyManager.CALL_STATE_IDLE){
                Toast.makeText(context, "idle", Toast.LENGTH_SHORT).show()
                Log.d("callState", "idle")
                if(contList.isNotEmpty()){
                    val contact = contList[0]
                    contList.removeAt(0)
                    callPhone(contact)
                }else{
                    telephonyManager.listen(null, LISTEN_NONE)
                }
            }else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                Toast.makeText(context, "offhook", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun sendAlertToAll(location: Location, call: Boolean){
        val contacts = dbHelper.getContacts()
        if (contacts != null) {
            for(i in contacts){
                if(i.isValid()){
                    sendSms(i, location)
                }
                if(call) {
//                    callPhone(i)
                    contList.add(i)
                }
            }
        }
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun callPhone(i: Contact) {
        val phoneCallIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${i.countryCode}${i.phoneNo}"))
        phoneCallIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, phoneCallIntent, null)
    }

    private fun sendSms(contact: Contact, location: Location){
        val msg = message + " " + mapsLink + location.latitude + "," + location.longitude
        smsManager.sendTextMessage(contact.countryCode + contact.phoneNo, null, msg, null, null)
    }
}
