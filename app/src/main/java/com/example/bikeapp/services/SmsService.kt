package com.example.bikeapp.services

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.Contact

class SmsService(context: Context) {
    private var message = "Test Accident alert\n"
    private var mapsLink = "https://www.google.com/maps/search/?api=1&query="
    private var smsManager: SmsManager = SmsManager.getDefault()
    private var dbHelper: DBHelper = DBHelper(context)

    fun sendAlertToAll(location: Location){
        val contacts = dbHelper.getContacts()
        if (contacts != null) {
            for(i in contacts){
                if(i.isValid()){
                    sendSms(i, location)
                }
            }
        }
    }

    private fun sendSms(contact: Contact, location: Location){
        val msg = message + " " + mapsLink + location.latitude + "," + location.longitude
        smsManager.sendTextMessage(contact.countryCode + contact.phoneNo, null, msg, null, null)
    }
}
