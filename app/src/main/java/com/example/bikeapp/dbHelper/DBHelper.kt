package com.example.bikeapp.dbHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

//    private var db: SQLiteDatabase? = null
    companion object{
        private var DATABASE_NAME = "emergencyContacts"
        private var DATABASE_VERSION = 1
        private var EMERGENCY_CONTACTS_TABLE_NAME = "emergencyContacts"
        private var COLUMN_ID = "id"
        private var COLUMN_CONTACT = "contact"
    }

    override fun onCreate(db: SQLiteDatabase?) {
//        this.db = db
        var createTable ="create table $EMERGENCY_CONTACTS_TABLE_NAME ($COLUMN_ID integer primary key, $COLUMN_CONTACT text)"
        db!!.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getContacts():List<String>{
        var contacts = ArrayList<String>()
        var query = "select * from $EMERGENCY_CONTACTS_TABLE_NAME"
        var cursor: Cursor? = null
        var dbl = readableDatabase
        try{
            cursor = dbl?.rawQuery(query, null)
        }catch (e: SQLiteException){
            Log.d("debug", "sqlite exception: " + e.message)
        }
        if(cursor!!.moveToFirst()){
            do{
                contacts.add(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT)))
            }while(cursor.moveToNext())
        }
        cursor.close()
        return contacts
    }

    fun insertContact(contact: String){
        var cv = ContentValues()
        cv.put(COLUMN_CONTACT, contact)
        var dbl = writableDatabase
        try{
            dbl.insert(EMERGENCY_CONTACTS_TABLE_NAME, null, cv)
        }catch (e: SQLiteException){
            Log.d("debug", "sqlite exception: " + e.message)
        }
    }

    fun updateContact(oldContact: String, newContact: String): Boolean{
        var cv = ContentValues()
        cv.put(COLUMN_CONTACT, newContact)
        var dbl = writableDatabase
        try {
            if(dbl.update(EMERGENCY_CONTACTS_TABLE_NAME, cv, COLUMN_CONTACT + "=" + oldContact, null) > 0){
                return true
            }
        }catch (e: Exception){
            Log.d("debug", "exception: " + e.message)
            return false
        }
        return false
    }

    fun deleteContact(contact: String): Boolean{
        var cv = ContentValues()
        cv.put(COLUMN_CONTACT, contact)
        var dbl = writableDatabase
        try{
            if(dbl.delete(EMERGENCY_CONTACTS_TABLE_NAME, COLUMN_CONTACT + "=" + contact, null) > 0){
                return true
            }
        }catch (e: Exception){
            Log.d("debug", "exception: " + e.message)
            return false
        }
        return false
    }

}