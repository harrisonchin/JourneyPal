package com.mobileinvalley.journeypal

import platform.CoreLocation.*
import platform.Foundation.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents

private class LocationManagerDelegate(
    private val onLocationUpdate: (GpsLocation?) -> Unit
) : NSObject(), CLLocationManagerDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        if (location != null) {
            val lat = location.coordinate.useContents { latitude }
            val lon = location.coordinate.useContents { longitude }
            onLocationUpdate(GpsLocation(lat, lon))
        } else {
            onLocationUpdate(null)
        }
        manager.stopUpdatingLocation()
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onLocationUpdate(null)
    }
}

private var activeLocationManager: CLLocationManager? = null
private var activeDelegate: LocationManagerDelegate? = null

actual suspend fun getCurrentLocation(): GpsLocation? = suspendCancellableCoroutine { continuation ->
    val locationManager = CLLocationManager()
    val delegate = LocationManagerDelegate { location ->
        activeLocationManager = null
        activeDelegate = null
        if (continuation.isActive) {
            continuation.resume(location)
        }
    }
    locationManager.delegate = delegate
    activeLocationManager = locationManager
    activeDelegate = delegate
    
    val status = CLLocationManager.authorizationStatus()
    if (status == kCLAuthorizationStatusNotDetermined) {
        locationManager.requestWhenInUseAuthorization()
    }
    
    locationManager.startUpdatingLocation()
    
    continuation.invokeOnCancellation {
        locationManager.stopUpdatingLocation()
        activeLocationManager = null
        activeDelegate = null
    }
}
