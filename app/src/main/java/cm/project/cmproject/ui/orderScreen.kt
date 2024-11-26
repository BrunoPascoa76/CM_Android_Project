package cm.project.cmproject.ui

import OrderViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.google.maps.android.compose.*

import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrderScreen(viewModel: OrderViewModel = viewModel()) {
    val orderState by viewModel.orderState.collectAsState()

    // Request location permissions dynamically
    val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Track the Order",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tracking Info
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(text = "Tracking Number: ${orderState.trackingNumber}")
            Text(text = "Driver: ${orderState.driverName}, ${orderState.driverPhone}")
        }

        // Status Flow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusItem("Driver Assigned", orderState.currentStatus == OrderStatus.DRIVER_ASSIGNED)
            StatusItem("Pickup", orderState.currentStatus == OrderStatus.PICKUP)
            StatusItem("In Transit", orderState.currentStatus == OrderStatus.IN_TRANSIT)
            StatusItem("Delivered", orderState.currentStatus == OrderStatus.DELIVERED)
        }

        // Google Map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            OrderMap(
                pickupLocation = orderState.pickupLocation,
                currentLocation = orderState.currentLocation,
                deliveryLocation = orderState.deliveryLocation
            )
        }
    }
}

@Composable
fun OrderMap(
    pickupLocation: LatLng,
    currentLocation: LatLng,
    deliveryLocation: LatLng
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 8f) // Center closer to Aveiro
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = true) ){
        // Pickup Marker
        Marker(
            state= MarkerState(position = pickupLocation),
            title = "Pickup Location",
            snippet = "Order starts here.",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )

        // Current Location Marker
        Marker(
            state= MarkerState(position = currentLocation),
            title = "Current Location",
            snippet = "Driver's current position.",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        )

        // Delivery Marker
        Marker(
            state= MarkerState(position = deliveryLocation),
            title = "Delivery Location",
            snippet = "Order will be delivered here.",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // Route Polyline
        Polyline(
            points = listOf(pickupLocation, currentLocation, deliveryLocation),
            color = androidx.compose.ui.graphics.Color.Black
        )
    }
}

@Composable
fun StatusItem(label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
    }
}