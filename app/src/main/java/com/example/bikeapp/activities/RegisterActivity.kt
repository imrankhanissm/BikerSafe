package com.example.bikeapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.Contact
import com.example.bikeapp.models.User
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_setting.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emergencyContactMandatoryRegister.findViewById<EditText>(R.id.moreContactPhoneRegister).hint= "Emergency Contact"

        addMoreEmergencyContactsRegister.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Add Contact")
            dialogBuilder.setNegativeButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("add") { dialog, _ ->
                val countryCode = (dialog as AlertDialog).findViewById<EditText>(R.id.countryCodeAddContactProfile)?.text.toString()
                val phoneNo = (dialog).findViewById<EditText>(R.id.phoneNoAddContactProfile)?.text.toString()

                val moreContactLayout = generateContactView(Contact(countryCode, phoneNo))
                emergencyContactListRegister.addView(moreContactLayout)
            }
            dialogBuilder.show()
        }

//        addMoreEmergencyContactsRegister.setOnClickListener{
//            if(emergencyContactListRegister.childCount > 1){
//                emergencyContactListRegister.removeViewAt(emergencyContactListRegister.childCount-1)
//            }
//        }

        register.setOnClickListener{
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            val myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
            if(usernameRegister.text.isNotEmpty()){
                val dbHelper = DBHelper(this)
                val prefEditor = myPrefs.edit()
                try {
                    prefEditor.putString(User.name, usernameRegister.text.toString())
                    prefEditor.putFloat(Constants.accelerationThreshold, Constants.accelerationThresholdDefault)
                    prefEditor.putFloat(Constants.gyroscopeThreshold, Constants.gyroscopeThresholdDefault)


                    for(i in 0 until emergencyContactListRegister.childCount){
                        if(((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text.isNotEmpty() &&
                            ((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(1) as EditText).text.isNotEmpty()){
                            val contact = Contact(((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text.toString(),
                                ((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(1) as EditText).text.toString())
                            dbHelper.insertContact(contact)
                        }
                    }

                    val contacts = dbHelper.getContacts()
                    if (contacts != null) {
                        for(i in contacts){
                            Log.d("debug", "emergency contact from database: $i")
                        }
                    }
                }catch (e: Exception){
                    Toast.makeText(this, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    dbHelper.close()
                }
                prefEditor.apply()
                startActivity(Intent(this, MapsActivity::class.java))
                finish()
            }
        }
    }

    private fun generateContactView(contact: Contact): View {
        val contactView = layoutInflater.inflate(R.layout.layout_contact, null)
        contactView.findViewById<TextView>(R.id.countryCodeProfile).text = contact.countryCode
        contactView.findViewById<TextView>(R.id.phoneNoProfile).text = contact.phoneNo
        contactView.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
            dialogView.findViewById<EditText>(R.id.countryCodeAddContactProfile)?.setText(contactView.findViewById<TextView>(R.id.countryCodeProfile).text)
            dialogView.findViewById<EditText>(R.id.phoneNoAddContactProfile)?.setText(contactView.findViewById<TextView>(R.id.phoneNoProfile).text)
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Edit Contact")
            dialogBuilder.setNegativeButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("save") { dialog, _ ->
                val newCountryCode = (dialog as AlertDialog).findViewById<EditText>(R.id.countryCodeAddContactProfile)?.text.toString()
                val newPhoneNo = (dialog).findViewById<EditText>(R.id.phoneNoAddContactProfile)?.text.toString()
                contactView.findViewById<TextView>(R.id.countryCodeProfile).text = newCountryCode
                contactView.findViewById<TextView>(R.id.phoneNoProfile).text = newPhoneNo
            }
            dialogBuilder.setNeutralButton("delete") { _, _ ->
                if(emergencyContactListRegister.childCount > 1){
                    emergencyContactListRegister.removeView(contactView)
                }else{
                    Toast.makeText(this, "Atleast one emergency contact required", Toast.LENGTH_SHORT).show()
                }
            }
            dialogBuilder.show()
        }
        return contactView
    }
}
