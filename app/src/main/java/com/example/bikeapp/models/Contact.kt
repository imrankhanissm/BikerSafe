package com.example.bikeapp.models

class Contact(var countryCode: String, var phoneNo: String) {
    companion object{
        const val countryCodeLabel = "countryCode"
        const val phoneNoLabel = "phoneNo"
    }

    fun isValid(): Boolean{
        if(phoneNo.length == 10 && countryCode == "+91"){
            return true
        }
        return false
    }
}