package com.mobileinvalley.journeypal

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
actual suspend fun getCurrentLocation(): GpsLocation? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    return try {
        val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        location?.let { GpsLocation(it.latitude, it.longitude) }
    } catch (e: Exception) {
        null
    }
}
