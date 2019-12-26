package com.example.bikeapp

object Constants {
    const val sharedPrefsName = "myPrefs"
    const val locationFromService = "com.example.bikerApp.services.sensorService.locationFromService"
    const val serviceStopped = "com.example.bikerApp.services.sensorService.serviceStopped"
    const val turnOffDriveMode = "com.example.bikerApp.services.sensorService.turnOffDriveMode"
    const val accelerationThreshold = "accelerationThreshold"
    const val accelerationThresholdDefault = 12F
    const val gyroscopeThreshold = "gyroscopeThreshold"
    const val gyroscopeThresholdDefault = 12F
    const val PENDING_INTENT_REQUEST_CODE_MAPS_ACTIVITY = 1
}