package cm.project.cmproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R
import cm.project.cmproject.components.DeliveryProgressBar
import cm.project.cmproject.models.OrderState
import cm.project.cmproject.models.OrderViewModel
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
    val coroutineScope = rememberCoroutineScope()
    // Request location permissions dynamically
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }
    Column(modifier = Modifier.padding(top = 30.dp)) {
        if (currentDeliveries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No deliveries found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        text = "Track your orders",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                HorizontalPager(
                    modifier = Modifier.weight(0.9f),
                    state = pagerState,
                    userScrollEnabled = false
                ) { index ->
                    orderPage(navController, orderState, deliveryHistoryViewModel, index)
                }
                if (currentDeliveries.size > 1) {
                    //add buttons to navigate between orders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (pagerState.currentPage > 0) {
                            ElevatedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            ) {
                                Text("Previous")
                            }
                        }
                        if (pagerState.currentPage < currentDeliveries.size - 1) {
                            ElevatedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            ) {
                                Text("Next")
                            }
                        }
                    }
                    DotsIndicator(
                        totalDots = currentDeliveries.size,
                        state = pagerState
                    )

                }
            }
        }
    }

}

@Composable
fun DotsIndicator(
    totalDots: Int,
    state: PagerState,
    modifier: Modifier = Modifier,
    selectedColor: Color = Color(0xFF4CAF50),
    unselectedColor: Color = Color(0xFFB0BEC5),
) {
    val selectedIndex = state.currentPage
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until totalDots) {
            val color = if (index == selectedIndex) selectedColor else unselectedColor
            Box(
                modifier = Modifier
                    .size(32.dp)
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentDelivery?.parcel?.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium
            )
        }

        DeliveryProgressBar(
            modifier = Modifier.padding(bottom = 10.dp),
            deliveryHistoryViewModel = deliveryHistoryViewModel,
            index = index
        )

        // Google Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .clip(MaterialTheme.shapes.medium)
        ) {
            OrderMap(
                pickupLocation = orderState.pickupLocation,
                currentLocation = orderState.currentLocation,
                deliveryLocation = orderState.deliveryLocation
            )
        }

        //TODO: hide this if not a driver or if there's no delivery
        ElevatedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
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
                    contentDescription = "View order details"
                )
                Text("View order details")
            }
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
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { /* Consume touch events and stay in map area */ }
            },
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