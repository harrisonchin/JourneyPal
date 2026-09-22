package com.mobileinvalley.journeypal.pro

data class GpsLocation(val latitude: Double, val longitude: Double)

expect suspend fun getCurrentLocation(): GpsLocation?
