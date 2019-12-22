package com.example.bikeapp.dbHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.bikeapp.models.Contact

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val context = context
    companion object {
        private var DATABASE_NAME = "emergencyContacts"
        private var DATABASE_VERSION = 1
        private var EMERGENCY_CONTACTS_TABLE_NAME = "emergencyContacts"
        private var COLUMN_ID = "id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
//        , constraint unique_constraint unique(${Contact.countryCodeLable}, ${Contact.phoneNoLabel})
        val createTable =
            "create table $EMERGENCY_CONTACTS_TABLE_NAME ($COLUMN_ID integer primary key autoincrement, ${Contact.countryCodeLable} text, ${Contact.phoneNoLabel} text)"
        db!!.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun getContacts(): List<Contact>? {
        val contacts = ArrayList<Contact>()
        val query = "select * from $EMERGENCY_CONTACTS_TABLE_NAME"
        var cursor: Cursor?
        val dbl = readableDatabase
        try {
            cursor = dbl?.rawQuery(query, null)
        } catch (e: SQLiteException) {
            dbl.close()
            Log.d("debug", "sqlite exception: " + e.message)
            return null
        }
        if (cursor!!.moveToFirst()) {
            do {
                contacts.add(Contact(cursor.getString(cursor.getColumnIndex(Contact.countryCodeLable)), cursor.getString(cursor.getColumnIndex(Contact.phoneNoLabel))))
            } while (cursor.moveToNext())
        }
        cursor.close()
        dbl.close()
        return contacts
    }

    fun insertContact(contact: Contact): Boolean {
        val cv = ContentValues()
        cv.put(Contact.countryCodeLable, contact.countryCode)
        cv.put(Contact.phoneNoLabel, contact.phoneNo)
        val dbl = writableDatabase
        try {
            if(dbl.insert(EMERGENCY_CONTACTS_TABLE_NAME, null, cv) > 0){
                dbl.close()
                return true
            }else{
                Toast.makeText(context, "Error inserting contact", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.d("debug", "sqlite exception: " + e.message)
            dbl.close()
            return false
        }
        dbl.close()
        return false
    }

    fun updateContact(oldContact: Contact, newContact: Contact): Boolean {
        val cv = ContentValues()
        cv.put(Contact.countryCodeLable, newContact.countryCode)
        cv.put(Contact.phoneNoLabel, newContact.phoneNo)
        val dbl = writableDatabase
        try {
            if (dbl.update(EMERGENCY_CONTACTS_TABLE_NAME, cv, Contact.countryCodeLable + " =? and " + Contact.phoneNoLabel + " =? ", arrayOf(oldContact.countryCode, oldContact.phoneNo)) > 0){
                dbl.close()
                return true
            }else{
                Toast.makeText(context, "Error updating contact", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.d("debug", "exception: " + e.message)
            dbl.close()
            return false
        }
        dbl.close()
        return false
    }

    fun deleteContact(contact: Contact): Boolean {
        val dbl = writableDatabase
        try {
            if (dbl.delete(EMERGENCY_CONTACTS_TABLE_NAME, Contact.countryCodeLable + " =? and " + Contact.phoneNoLabel + " =? ", arrayOf(contact.countryCode, contact.phoneNo)) > 0) {
                dbl.close()
                return true
            }else{
                Toast.makeText(context, "Error deleting contact", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.d("debug", "exception: " + e.message)
            dbl.close()
            return false
        }
        dbl.close()
        return false
    }

}