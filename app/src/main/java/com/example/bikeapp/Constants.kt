package com.example.bikeapp

object Constants {
    const val sharedPrefsName = "myPrefs"
    const val locationFromService = "com.example.bikerApp.services.sensorService.locationFromService"
    const val serviceStopped = "com.example.bikerApp.services.sensorService.serviceStopped"
    const val turnOffDriveMode = "com.example.bikerApp.services.sensorService.turnOffDriveMode"
    const val callContacts = "com.example.bikerApp.services.sensorService.callContacts"
    const val PENDING_INTENT_REQUEST_CODE_MAPS_ACTIVITY = 1

    object Settings{
        const val username = "username"
        const val accelerationThreshold = "accelerationThreshold"
        const val accelerationThresholdDefault = 12F
        const val gyroscopeThreshold = "gyroscopeThreshold"
        const val gyroscopeThresholdDefault = 12F
        const val countDownTime = "countDownTime"
        const val countDownTimeDefault = 15
        const val sound = "sound"
        const val vibrate = "vibrate"
        const val call = "call"
        const val message = "message"
    }

    object NotificationChannels {
        const val channel1 = "drive mode"
    }
}