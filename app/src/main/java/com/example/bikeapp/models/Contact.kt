package com.example.bikeapp.models

class Contact(countryCode: String, phoneNo: String) {
    companion object{
        const val countryCodeLable = "countryCode"
        const val phoneNoLabel = "phoneNo"
    }
    var countryCode: String = countryCode
    var phoneNo: String = phoneNo
}