package com.mobileinvalley.journeypal

data class GpsLocation(val latitude: Double, val longitude: Double)

expect suspend fun getCurrentLocation(): GpsLocation?
