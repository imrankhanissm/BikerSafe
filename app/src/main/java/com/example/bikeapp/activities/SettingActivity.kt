package com.example.bikeapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.Contact
import com.example.bikeapp.services.SensorService
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private lateinit var myPrefs: SharedPreferences
    private lateinit var dbHelper: DBHelper
    private val RESULT_PICK_CONTACT = 1
    private lateinit var dialogView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Setting"
        myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        usernameProfile.text = myPrefs.getString(Constants.Settings.username, "Username")

        usernameProfileContainer.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
            dialogView.findViewById<EditText>(R.id.mapsMenuProfileEdit)?.setText(usernameProfile.text)
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Edit Name")
            dialogBuilder.setNegativeButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("save") { dialog, _ ->
                val newName = (dialog as AlertDialog).findViewById<EditText>(R.id.mapsMenuProfileEdit)?.text.toString()
                Log.d("debug", newName)

                myPrefs.edit().putString(Constants.Settings.username, newName).apply()
                usernameProfile.text = newName
            }
            dialogBuilder.show()
        }

        dbHelper = DBHelper(this)
        val contacts = dbHelper.getContacts()

        if (contacts != null) {
            for (i in contacts){
                val contactView = generateContactView(i)
                emergencyContactListProfile.addView(contactView)
            }
        }

        addMoreEmergencyContactsProfile.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
            dialogView.findViewById<ImageView>(R.id.contactsButton).setOnClickListener {
                val contactsIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                startActivityForResult(contactsIntent, RESULT_PICK_CONTACT)
            }
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Add Contact")
            dialogBuilder.setNegativeButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("add") { dialog, _ ->
                val countryCode = (dialog as AlertDialog).findViewById<EditText>(R.id.countryCodeAddContactProfile)?.text.toString()
                val phoneNo = (dialog).findViewById<EditText>(R.id.phoneNoAddContactProfile)?.text.toString()
                val contact = Contact(countryCode, phoneNo)
                if(dbHelper.insertContact(contact)){
                    val contactView = generateContactView(contact)
                    emergencyContactListProfile.addView(contactView)
                }
            }
            dialogBuilder.show()
        }

        val accelerationThreshold = myPrefs.getFloat(Constants.Settings.accelerationThreshold, Constants.Settings.accelerationThresholdDefault)
        accelerationThresholdSetting.text = String.format("%d " + getString(R.string.m_s2), accelerationThreshold.toInt())
        accelerationSeekBarSetting.progress = accelerationThreshold.toInt()

        accelerationSeekBarSetting.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                accelerationThresholdSetting.text = String.format("%d " + getString(R.string.m_s2), progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    myPrefs.edit().putFloat(Constants.Settings.accelerationThreshold, seekBar.progress.toFloat()).apply()
                    SensorService.accelerationThreshold = seekBar.progress.toFloat()
                }
            }
        })

        soundSwitch.isChecked = myPrefs.getBoolean(Constants.Settings.sound, true)
        vibrateSwitch.isChecked = myPrefs.getBoolean(Constants.Settings.vibrate, true)
        callSwitch.isChecked = myPrefs.getBoolean(Constants.Settings.call, true)

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                myPrefs.edit().putBoolean(Constants.Settings.sound, true).apply()
            }else{
                myPrefs.edit().putBoolean(Constants.Settings.sound, false).apply()
            }
        }

        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                myPrefs.edit().putBoolean(Constants.Settings.vibrate, true).apply()
            }else{
                myPrefs.edit().putBoolean(Constants.Settings.vibrate, false).apply()
            }
        }

        callSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                myPrefs.edit().putBoolean(Constants.Settings.call, true).apply()
            }else{
                myPrefs.edit().putBoolean(Constants.Settings.call, false).apply()
            }
        }

        val countDownTime = myPrefs.getInt(Constants.Settings.countDownTime, Constants.Settings.countDownTimeDefault)
        countDownTimeSeekBarValue.text = "$countDownTime secs"
        countDownTimeSeekBar.progress = (countDownTime*100)/120
        countDownTimeSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val waitTime = (progress * 120) / 100
                countDownTimeSeekBarValue.text = "$waitTime secs"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    myPrefs.edit().putInt(Constants.Settings.countDownTime, (seekBar.progress*120)/100).apply()
                }
            }
        })

        message.text = myPrefs.getString(Constants.Settings.message, "Test Accident alert")
        messageContainer.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
            dialogView.findViewById<EditText>(R.id.mapsMenuProfileEdit)?.setText(message.text)
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Edit Message")
            dialogBuilder.setNegativeButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("save") { dialog, _ ->
                val newMessage = (dialog as AlertDialog).findViewById<EditText>(R.id.mapsMenuProfileEdit)?.text.toString()
                Log.d("debug", newMessage)

                myPrefs.edit().putString(Constants.Settings.message, newMessage).apply()
                message.text = newMessage
            }
            dialogBuilder.show()
        }
    }

    private fun generateContactView(contact: Contact): View {
        val contactView = layoutInflater.inflate(R.layout.layout_contact, null)
        contactView.findViewById<TextView>(R.id.countryCodeProfile).text = contact.countryCode
        contactView.findViewById<TextView>(R.id.phoneNoProfile).text = contact.phoneNo
        contactView.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
            dialogView.findViewById<EditText>(R.id.countryCodeAddContactProfile)?.setText(contactView.findViewById<TextView>(R.id.countryCodeProfile).text)
            dialogView.findViewById<EditText>(R.id.phoneNoAddContactProfile)?.setText(contactView.findViewById<TextView>(R.id.phoneNoProfile).text)
            dialogView.findViewById<ImageView>(R.id.contactsButton).setOnClickListener {
                val contactsIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                startActivityForResult(contactsIntent, RESULT_PICK_CONTACT)
            }

            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Edit Contact")
            dialogBuilder.setNegativeButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("save") { dialog, _ ->
                val oldCountryCode = contactView.findViewById<TextView>(R.id.countryCodeProfile)?.text.toString()
                val oldPhoneNo = contactView.findViewById<TextView>(R.id.phoneNoProfile)?.text.toString()

                val newCountryCode = (dialog as AlertDialog).findViewById<EditText>(R.id.countryCodeAddContactProfile)?.text.toString()
                val newPhoneNo = (dialog).findViewById<EditText>(R.id.phoneNoAddContactProfile)?.text.toString()

                if(dbHelper.updateContact(Contact(oldCountryCode, oldPhoneNo), Contact(newCountryCode, newPhoneNo))){
                    contactView.findViewById<TextView>(R.id.countryCodeProfile).text = newCountryCode
                    contactView.findViewById<TextView>(R.id.phoneNoProfile).text = newPhoneNo
                }
            }
            dialogBuilder.setNeutralButton("delete") { _, _ ->
                val newCountryCode = contactView.findViewById<TextView>(R.id.countryCodeProfile)?.text.toString()
                val newPhoneNo = contactView.findViewById<TextView>(R.id.phoneNoProfile)?.text.toString()
                if(emergencyContactListProfile.childCount > 1){
                    if(dbHelper.deleteContact(Contact(newCountryCode, newPhoneNo))){
                        emergencyContactListProfile.removeView(contactView)
                    }
                }else{
                    Toast.makeText(this, "At least one emergency contact required", Toast.LENGTH_SHORT).show()
                }
            }
            dialogBuilder.show()
        }
        return contactView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RESULT_PICK_CONTACT){
            try {
                val uri = data?.data
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor!!.moveToFirst()
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val contactName = cursor.getString(nameIndex)
                val contactNumber = cursor.getString(phoneIndex)
                var cleanedContactNumber = ""
                for (i in contactNumber.length-1 downTo 0){
                    if(contactNumber[i].isDigit()){
                        cleanedContactNumber = contactNumber[i] + cleanedContactNumber
                        if(cleanedContactNumber.length == 10){
                            break
                        }
                    }
                }
                if(dialogView.isShown){
                    dialogView.findViewById<EditText>(R.id.phoneNoAddContactProfile).setText(cleanedContactNumber)
                }
                Log.d("debug", "$contactName -> $cleanedContactNumber")
            }catch (exception: Exception){
                Log.d("debug", "Exception while picking contact: " + exception.message)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}