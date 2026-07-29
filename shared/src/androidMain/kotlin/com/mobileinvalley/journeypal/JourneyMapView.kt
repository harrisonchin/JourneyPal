package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun JourneyMapView(
    items: List<JourneyItem>,
    modifier: Modifier
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
        items.forEach { item ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(item.latitude, item.longitude)
            marker.title = item.notes
            mapView.overlays.add(marker)
        }
        
        if (items.isNotEmpty()) {
            val firstItem = items.first()
            mapView.controller.setZoom(10.0)
            mapView.controller.setCenter(GeoPoint(firstItem.latitude, firstItem.longitude))
        }

        onDispose {
            // Optional: clean up
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
