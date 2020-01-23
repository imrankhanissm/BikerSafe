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
import com.example.bikeapp.models.User
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
        usernameProfile.text = myPrefs.getString(User.name, null)

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

                myPrefs.edit().putString(User.name, newName).apply()
                usernameProfile.text = newName
            }
            dialogBuilder.show()
        }

        dbHelper = DBHelper(this)
        var contacts = dbHelper.getContacts()

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
                var contactsIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
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

        var accelerationThreshold = myPrefs.getFloat(Constants.accelerationThreshold, Constants.accelerationThresholdDefault)
        accelerationThresholdSetting.text = accelerationThreshold.toString() + "G"
        accelerationSeekBarSetting.progress = accelerationThreshold.toInt()

        accelerationSeekBarSetting.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                accelerationThresholdSetting.text = progress.toString() + "G"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    myPrefs.edit().putFloat(Constants.accelerationThreshold, seekBar.progress.toFloat()).apply()
                    SensorService.accelerationThreshold = seekBar.progress.toFloat()
                }
            }
        })
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
                var contactsIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
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
                var uri = data?.data
                var cursor = contentResolver.query(uri, null, null, null, null)
                cursor.moveToFirst()
                var nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                var phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                var contactName = cursor.getString(nameIndex)
                var contactNumber = cursor.getString(phoneIndex)
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