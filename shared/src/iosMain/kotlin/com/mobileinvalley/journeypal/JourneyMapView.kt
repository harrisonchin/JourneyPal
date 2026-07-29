package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.CoreLocation.CLLocationCoordinate2DMake

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun JourneyMapView(
    items: List<JourneyItem>,
    modifier: Modifier
) {
    val mapView = remember { MKMapView() }

    UIKitView(
        factory = {
            mapView
        },
        modifier = modifier,
        update = { view ->
            view.removeAnnotations(view.annotations)
            items.forEach { item ->
                val annotation = MKPointAnnotation()
                annotation.setCoordinate(CLLocationCoordinate2DMake(item.latitude, item.longitude))
                annotation.setTitle(item.notes)
                view.addAnnotation(annotation)
            }
            
            if (items.isNotEmpty()) {
                val firstItem = items.first()
                val center = CLLocationCoordinate2DMake(firstItem.latitude, firstItem.longitude)
                val region = MKCoordinateRegionMakeWithDistance(center, 10000.0, 10000.0)
                view.setRegion(region, animated = true)
            }
        }
    )
}
