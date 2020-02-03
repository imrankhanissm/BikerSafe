package com.example.bikeapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.Contact
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val RESULT_PICK_CONTACT = 1
    private lateinit var dialogView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        addMoreEmergencyContactsRegister.setOnClickListener {
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

                val moreContactLayout = generateContactView(Contact(countryCode, phoneNo))
                emergencyContactListRegister.addView(moreContactLayout)
            }
            dialogBuilder.show()
        }

        register.setOnClickListener{
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            val myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
            if(usernameRegister.text.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if((emergencyContactListRegister.childCount == 0)){
                Toast.makeText(this, "At least one emergency contact required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dbHelper = DBHelper(this)
            val prefEditor = myPrefs.edit()
            try {
                prefEditor.putString(Constants.Settings.username, usernameRegister.text.toString())
                prefEditor.putFloat(Constants.Settings.accelerationThreshold, Constants.Settings.accelerationThresholdDefault)
                prefEditor.putFloat(Constants.Settings.gyroscopeThreshold, Constants.Settings.gyroscopeThresholdDefault)
                prefEditor.putInt(Constants.Settings.countDownTime, Constants.Settings.countDownTimeDefault)
                prefEditor.putBoolean(Constants.Settings.sound, true)
                prefEditor.putBoolean(Constants.Settings.vibrate, true)
                prefEditor.putBoolean(Constants.Settings.call, false)


                for(i in 0 until emergencyContactListRegister.childCount){
                    if((((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text.isNotEmpty() &&
                        (((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.isNotEmpty()){
                        val contact = Contact((((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text.toString(),
                            (((emergencyContactListRegister.getChildAt(i) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString())
                        dbHelper.insertContact(contact)
                    }
                }

                prefEditor.apply()
                startActivity(Intent(this, MapsActivity::class.java))
                finish()
            }catch (e: Exception){
                Toast.makeText(this, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                dbHelper.close()
            }
        }
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
                cursor.close()
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
            }catch (exception: Exception){
                Log.d("debug", "Exception while picking contact: " + exception.message)
                Toast.makeText(this, "Error while registering", Toast.LENGTH_SHORT).show()
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
                emergencyContactListRegister.removeView(contactView)
            }
            dialogBuilder.show()
        }
        return contactView
    }
}
