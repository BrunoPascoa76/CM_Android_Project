package cm.project.cmproject.ui

import OrderState
import OrderViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R
import cm.project.cmproject.components.DeliveryProgressBar
import cm.project.cmproject.viewModels.DeliveryHistoryViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrderScreen(
    mockViewModel: OrderViewModel = viewModel(),
    deliveryHistoryViewModel: DeliveryHistoryViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val orderState by mockViewModel.orderState.collectAsState()
    val currentDeliveries by deliveryHistoryViewModel.currentDeliveries.collectAsState()
    val pagerState = rememberPagerState(pageCount = { currentDeliveries.size })

    // Request location permissions dynamically
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }
    if (currentDeliveries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No deliveries found")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HorizontalPager(
                modifier = Modifier.weight(0.9f),
                state = pagerState,
            ) { index ->
                orderPage(navController, orderState, deliveryHistoryViewModel, index)
            }
            if (currentDeliveries.size > 1) {
                DotsIndicator(
                    totalDots = currentDeliveries.size,
                    state = pagerState
                )
            }
        }
    }
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    state: PagerState,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.secondary,
) {
    val selectedIndex = state.currentPage
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until totalDots) {
            val color = if (index == selectedIndex) selectedColor else unselectedColor
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .padding(horizontal = 5.dp, vertical = 5.dp)
                    .background(color, shape = CircleShape)
                    .clickable {
                        coroutineScope.launch { state.animateScrollToPage(index) }
                    }
            )
        }
    }
}


@Composable
private fun orderPage(
    navController: NavController,
    orderState: OrderState,
    deliveryHistoryViewModel: DeliveryHistoryViewModel,
    index: Int = 0
) {
    val currentDelivery = deliveryHistoryViewModel.currentDeliveries.value.getOrNull(index)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Track the Order",
                style = MaterialTheme.typography.titleLarge
            )
            //TODO: hide this if not a driver or if there's no delivery
            ElevatedButton(
                enabled = currentDelivery != null,
                onClick = {
                    navController.navigate("deliveryDetails/${currentDelivery?.deliveryId}")
                }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.visibility_24px),
                        contentDescription = "See more"
                    )
                    Text("See more")
                }
            }
        }

        DeliveryProgressBar(
            modifier = Modifier.padding(bottom = 10.dp),
            deliveryHistoryViewModel = deliveryHistoryViewModel,
            index = index
        )

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
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        // Pickup Marker
        Marker(
            state = MarkerState(position = pickupLocation),
            title = "Pickup Location",
            snippet = "Order starts here.",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )

        // Current Location Marker
        Marker(
            state = MarkerState(position = currentLocation),
            title = "Current Location",
            snippet = "Driver's current position.",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        )

        // Delivery Marker
        Marker(
            state = MarkerState(position = deliveryLocation),
            title = "Delivery Location",
            snippet = "Order will be delivered here.",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // Route Polyline
        Polyline(
            points = listOf(pickupLocation, currentLocation, deliveryLocation),
            color = Color.Black
        )
    }
}