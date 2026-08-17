package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.MapKit.*
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.*
import platform.Foundation.*
import platform.objc.sel_registerName
import platform.darwin.NSObject
import platform.CoreGraphics.CGRectMake

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun JourneyMapView(
    items: List<JourneyItem>,
    modifier: Modifier,
    onItemClick: (JourneyItem) -> Unit
) {
    val delegate = remember(items, onItemClick) {
        object : NSObject(), MKMapViewDelegateProtocol {
            override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
                if (viewForAnnotation is MKUserLocation) return null
                
                val identifier = "JourneyMarker"
                var annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier) as? MKMarkerAnnotationView
                
                if (annotationView == null) {
                    annotationView = MKMarkerAnnotationView(viewForAnnotation, identifier)
                    annotationView.canShowCallout = true
                    annotationView.rightCalloutAccessoryView = UIButton.buttonWithType(UIButtonTypeDetailDisclosure)
                } else {
                    annotationView.annotation = viewForAnnotation
                }
                
                val annotation = viewForAnnotation as? JourneyAnnotation
                val item = annotation?.item
                
                if (item != null && item.photoUris.isNotEmpty()) {
                    val imageView = UIImageView(frame = CGRectMake(0.0, 0.0, 40.0, 40.0))
                    imageView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
                    imageView.clipsToBounds = true
                    
                    val uri = item.photoUris.first()
                    val resolved = resolveUri(uri)
                    if (resolved is String) {
                        val nsUrl = NSURL.URLWithString(resolved)
                        if (nsUrl != null) {
                            val data = NSData.dataWithContentsOfURL(nsUrl)
                            if (data != null) {
                                imageView.image = UIImage.imageWithData(data)
                            }
                        }
                    }
                    annotationView.leftCalloutAccessoryView = imageView
                } else {
                    annotationView.leftCalloutAccessoryView = null
                }
                
                return annotationView
            }
            
            override fun mapView(mapView: MKMapView, annotationView: MKAnnotationView, calloutAccessoryControlTapped: UIControl) {
                val annotation = annotationView.annotation as? JourneyAnnotation
                val item = annotation?.item
                if (item != null) {
                    onItemClick(item)
                }
            }
        }
    }

    val mapView = remember { 
        MKMapView().apply {
            setDelegate(delegate)
        }
    }

    UIKitView(
        factory = {
            mapView
        },
        modifier = modifier,
        update = { view ->
            view.removeAnnotations(view.annotations)
            items.forEach { item ->
                val annotation = JourneyAnnotation(item)
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

@OptIn(ExperimentalForeignApi::class)
class JourneyAnnotation(val item: JourneyItem) : MKPointAnnotation() {
    init {
        setCoordinate(CLLocationCoordinate2DMake(item.latitude, item.longitude))
        setTitle(item.notes)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LocationPickerMapView(
    initialLatitude: Double,
    initialLongitude: Double,
    modifier: Modifier,
    onLocationSelected: (Double, Double) -> Unit
) {
    val annotation = remember { MKPointAnnotation() }
    
    val mapView = remember {
        MKMapView().apply {
            val center = CLLocationCoordinate2DMake(initialLatitude, initialLongitude)
            val region = MKCoordinateRegionMakeWithDistance(center, 1000.0, 1000.0)
            setRegion(region, animated = false)
            
            annotation.setCoordinate(center)
            addAnnotation(annotation)
        }
    }

    val gestureRecognizer = remember {
        val recognizer = object : NSObject() {
            @Suppress("UNUSED_PARAMETER")
            @OptIn(BetaInteropApi::class)
            @ObjCAction
            fun handleTap(sender: UITapGestureRecognizer) {
                if (sender.state == UIGestureRecognizerStateEnded) {
                    val location = sender.locationInView(mapView)
                    val coordinate = mapView.convertPoint(location, toCoordinateFromView = mapView)
                    
                    coordinate.useContents {
                        val lat = this.latitude
                        val lon = this.longitude
                        annotation.setCoordinate(CLLocationCoordinate2DMake(lat, lon))
                        onLocationSelected(lat, lon)
                    }
                }
            }
        }
        
        UITapGestureRecognizer(target = recognizer, action = sel_registerName("handleTap:"))
    }

    DisposableEffect(mapView) {
        mapView.addGestureRecognizer(gestureRecognizer)
        onDispose {
            mapView.removeGestureRecognizer(gestureRecognizer)
        }
    }

    UIKitView(
        factory = { mapView },
        modifier = modifier
    )
}
