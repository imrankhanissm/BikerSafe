package com.example.bikeapp.activities

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bikeapp.Constants
import com.example.bikeapp.R
import com.example.bikeapp.dbHelper.DBHelper
import com.example.bikeapp.models.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var myPrefs: SharedPreferences
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        myPrefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE)
        usernameProfile.text = myPrefs.getString(User.name, null)
        bloodGroupProfile.setText(myPrefs.getString(User.bloodGroup, null))

        usernameProfileContainer.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
            dialogView.findViewById<EditText>(R.id.mapsMenuProfileEdit)?.setText(usernameProfile.text)
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Edit Name")
            dialogBuilder.setNegativeButton("cancel") { dialog, which ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("save") { dialog, which ->
                val newName = (dialog as AlertDialog).findViewById<EditText>(R.id.mapsMenuProfileEdit)?.text.toString()
                Log.d("debug", newName)

                myPrefs.edit().putString(User.name, newName).apply()
                usernameProfile.text = newName
            }
            dialogBuilder.show()
        }

        bloodGroupProfileContainer.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
            dialogView.findViewById<EditText>(R.id.mapsMenuProfileEdit)?.setText(bloodGroupProfile.text)
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Edit Blood Group")
            dialogBuilder.setNegativeButton("cancel") { dialog, which ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("save") { dialog, which ->
                val newName = (dialog as AlertDialog).findViewById<EditText>(R.id.mapsMenuProfileEdit)?.text.toString()
                Log.d("debug", newName)

                myPrefs.edit().putString(User.bloodGroup, newName).apply()
                bloodGroupProfile.text = newName

            }
            dialogBuilder.show()
        }

        dbHelper = DBHelper(this)
        var contacts = dbHelper.getContacts()

        for (i in contacts){
            val contact = TextView(this)
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            contact.layoutParams = layoutParams
            contact.text = i
            contact.textSize = 20F
            contact.setPadding(8, 8, 8, 40)
            contact.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_black_24dp, 0)
            contact.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
                dialogView.findViewById<EditText>(R.id.mapsMenuProfileEdit)?.setText(contact.text)
                dialogBuilder.setView(dialogView)
                dialogBuilder.setTitle("Edit Contact")
                dialogBuilder.setNegativeButton("cancel") { dialog, which ->
                    dialog.dismiss()
                }
                dialogBuilder.setPositiveButton("save") { dialog, which ->
                    val newName = (dialog as AlertDialog).findViewById<EditText>(R.id.mapsMenuProfileEdit)?.text.toString()
                    Log.d("debug", newName)

                    dbHelper.updateContact(contact.text.toString(), newName)
                    contact.text = newName
                }
                dialogBuilder.setNeutralButton("delete") { dialog, which ->
                    if(emergencyContactListProfile.childCount > 1){
                        Log.d("debug", "delete contact")
                        dbHelper.deleteContact(contact.text.toString())
                        emergencyContactListProfile.removeView(contact)
                    }else{
                        Toast.makeText(this, "Atleast one emergency contact required", Toast.LENGTH_SHORT).show()
                    }
                }
                dialogBuilder.show()
            }
            emergencyContactListProfile.addView(contact)
        }

        addMoreEmergencyContactsProfile.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
            dialogBuilder.setView(dialogView)
            dialogBuilder.setTitle("Add Contact")
            dialogBuilder.setNegativeButton("cancel") { dialog, which ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("add") { dialog, which ->
                val newContact = (dialog as AlertDialog).findViewById<EditText>(R.id.mapsMenuProfileEdit)?.text.toString()
                Log.d("debug", newContact)

                dbHelper.insertContact(newContact)
                val contact = TextView(this)
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                contact.layoutParams = layoutParams
                contact.text = newContact
                contact.textSize = 20F
                contact.setPadding(8, 8, 8, 40)
                contact.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_black_24dp, 0)

                contact.setOnClickListener {
                    val dialogBuilder = AlertDialog.Builder(this)
                    val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
                    dialogView.findViewById<EditText>(R.id.mapsMenuProfileEdit)?.setText(contact.text)
                    dialogBuilder.setView(dialogView)
                    dialogBuilder.setTitle("Edit Contact")
                    dialogBuilder.setNegativeButton("cancel", DialogInterface.OnClickListener{ dialog, which ->
                        dialog.dismiss()
                    })
                    dialogBuilder.setPositiveButton("save", DialogInterface.OnClickListener { dialog, which ->
                        val newName = (dialog as AlertDialog).findViewById<EditText>(R.id.mapsMenuProfileEdit)?.text.toString()
                        Log.d("debug", newName)

                        dbHelper.updateContact(contact.text.toString(), newName)
                        contact.text = newName
                    })
                    dialogBuilder.setNeutralButton("delete", DialogInterface.OnClickListener { dialog, which ->
                        if(emergencyContactListProfile.childCount > 1){
                            Log.d("debug", "delete contact")
                            dbHelper.deleteContact(contact.text.toString())
                            emergencyContactListProfile.removeView(contact)
                        }else{
                            Toast.makeText(this, "Atleast one emergency contact required", Toast.LENGTH_SHORT).show()
                        }
                    })
                    dialogBuilder.show()
                }
                emergencyContactListProfile.addView(contact)
            }
            dialogBuilder.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
