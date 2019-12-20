package com.example.bikeapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.User
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        addMoreEmergencyContacts.setOnClickListener {
            val moreContact = EditText(this)
            moreContact.hint = "Emergency Contact " + (emergencyContactListRegister.childCount + 1)
            moreContact.layoutParams =
                ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            emergencyContactListRegister.addView(moreContact)
            moreContact.requestFocus()
        }

        removeMoreEmergencyContacts.setOnClickListener{
            if(emergencyContactListRegister.childCount > 1){
                emergencyContactListRegister.removeViewAt(emergencyContactListRegister.childCount-1)
            }
        }

        register.setOnClickListener{
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            val myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
            if(usernameRegister.text.isNotEmpty() and bloodGroupRegister.text.isNotEmpty() and emergencyContact1.text.isNotEmpty()){
                val dbHelper = DBHelper(this)
                val prefEditor = myPrefs.edit()
                try {
                    prefEditor.putString(User.name, usernameRegister.text.toString())
                    prefEditor.putString(User.bloodGroup, bloodGroupRegister.text.toString())


                    for(i in 0 until emergencyContactListRegister.childCount){
                        if((emergencyContactListRegister.getChildAt(i) as EditText).text.isNotEmpty()){
                            dbHelper.insertContact((emergencyContactListRegister.getChildAt(i) as EditText).text.toString())
                        }
                    }

                    val contacts = dbHelper.getContacts()
                    for(i in contacts){
                        Log.d("debug", "emergency contact from database: $i")
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
}
