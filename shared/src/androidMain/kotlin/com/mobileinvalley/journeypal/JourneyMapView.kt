package com.mobileinvalley.journeypal

import android.widget.ImageView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.osmdroid.config.Configuration
import org.osmdroid.library.R as osmdroidR
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

@Composable
actual fun JourneyMapView(
    items: List<JourneyItem>,
    modifier: Modifier,
    onItemClick: (JourneyItem) -> Unit
) {
    val context = LocalContext.current
    
    // osmdroid configuration
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(items) {
        mapView.overlays.clear()
        items.forEach { journeyItem ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(journeyItem.latitude, journeyItem.longitude)
            marker.title = journeyItem.notes
            marker.snippet = "Lat: ${journeyItem.latitude}, Lon: ${journeyItem.longitude}"
            
            // Set a custom info window
            marker.infoWindow = object : MarkerInfoWindow(osmdroidR.layout.bonuspack_bubble, mapView) {
                override fun onOpen(item: Any?) {
                    super.onOpen(item)
                    val m = item as? Marker ?: return
                    val bubbleView = view
                    
                    // Manually populate image since super.onOpen might fail to find the ID if package naming is off
                    // Look for common ID names used in osmdroid/bonuspack bubbles
                    val imageId = bubbleView?.context?.resources?.getIdentifier("bubble_image", "id", bubbleView.context.packageName) ?: 0
                    val imageView = if (imageId != 0) bubbleView?.findViewById<ImageView>(imageId) else {
                        bubbleView?.findViewById<ImageView>(osmdroidR.id.bubble_image)
                    }
                    
                    if (m.image != null && imageView != null) {
                        imageView.setImageDrawable(m.image)
                        imageView.visibility = android.view.View.VISIBLE
                    }
                    
                    bubbleView?.setOnClickListener {
                        onItemClick(journeyItem)
                    }
                }
            }

            // Load thumbnail if present
            if (journeyItem.photoUris.isNotEmpty()) {
                val uri = journeyItem.photoUris.first()
                val request = ImageRequest.Builder(context)
                    .data(resolveUri(uri))
                    .crossfade(true)
                    .target { image ->
                        val drawable = image.asDrawable(context.resources)
                        marker.image = drawable
                        
                        // If the info window is already open, update it directly
                        if (marker.isInfoWindowShown) {
                            val infoWindow = marker.infoWindow as? MarkerInfoWindow
                            val bubbleView = infoWindow?.view
                            val imageId = bubbleView?.context?.resources?.getIdentifier("bubble_image", "id", bubbleView.context.packageName) ?: 0
                            val imageView = if (imageId != 0) bubbleView?.findViewById<ImageView>(imageId) else {
                                bubbleView?.findViewById<ImageView>(osmdroidR.id.bubble_image)
                            }
                            
                            if (imageView != null) {
                                imageView.setImageDrawable(drawable)
                                imageView.visibility = android.view.View.VISIBLE
                            }
                        }
                    }
                    .build()
                ImageLoader(context).enqueue(request)
            }

            mapView.overlays.add(marker)
        }
        
        if (items.isNotEmpty()) {
            val firstItem = items.first()
            mapView.controller.setZoom(10.0)
            mapView.controller.setCenter(GeoPoint(firstItem.latitude, firstItem.longitude))
        }

        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

@Composable
actual fun LocationPickerMapView(
    initialLatitude: Double,
    initialLongitude: Double,
    modifier: Modifier,
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    val marker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
    }

    DisposableEffect(initialLatitude, initialLongitude) {
        mapView.overlays.clear()
        
        marker.position = GeoPoint(initialLatitude, initialLongitude)
        mapView.overlays.add(marker)
        
        val eventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                marker.position = p
                onLocationSelected(p.latitude, p.longitude)
                mapView.invalidate()
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
        
        mapView.overlays.add(org.osmdroid.views.overlay.MapEventsOverlay(eventsReceiver))
        
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(initialLatitude, initialLongitude))

        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
