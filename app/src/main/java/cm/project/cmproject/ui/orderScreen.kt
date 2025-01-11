package cm.project.cmproject.ui

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R
import cm.project.cmproject.components.DeliveryProgressBar
import cm.project.cmproject.utils.ManifestUtils
import cm.project.cmproject.viewModels.DeliveryHistoryViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrderScreen(
    deliveryHistoryViewModel: DeliveryHistoryViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val currentDeliveries by deliveryHistoryViewModel.currentDeliveries.collectAsState()
    val pagerState = rememberPagerState(pageCount = { currentDeliveries.size })
    val coroutineScope = rememberCoroutineScope()
    // Request location permissions dynamically
    val locationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

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
                    orderPage(navController, deliveryHistoryViewModel, index)
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
    deliveryHistoryViewModel: DeliveryHistoryViewModel,
    index: Int = 0
) {
    val context = LocalContext.current
    val currentDelivery = deliveryHistoryViewModel.currentDeliveries.value[index]
    var pickupLocation by remember { mutableStateOf<LatLng?>(null) }
    var deliveryLocation by remember { mutableStateOf<LatLng?>(null) }
    LaunchedEffect(currentDelivery) {
        currentDelivery.let { delivery ->
            getLatLngFromAddress(context, delivery.fromAddress) { location ->
                pickupLocation = location
            }
            getLatLngFromAddress(context, delivery.toAddress) { location ->
                deliveryLocation = location
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentDelivery.parcel.label.let {
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
        if (pickupLocation != null && deliveryLocation != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                OrderMap(
                    pickupLocation = pickupLocation!!,
                    deliveryLocation = deliveryLocation!!,
                    context = context
                )
            }
        }

        ElevatedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            onClick = {
                navController.navigate("deliveryDetails/${currentDelivery.deliveryId}")
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
    context: Context,
    pickupLocation: LatLng,
    deliveryLocation: LatLng,
    currentLocation: LatLng = LatLng(40.6405, -8.6538)
) {
    var isLoading by remember { mutableStateOf(true) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 8f)
    }

    LaunchedEffect(pickupLocation, currentLocation, deliveryLocation) {
        withContext(Dispatchers.IO) {
            var geoContext: GeoApiContext? = null
            try {
                isLoading = true
                val apiKey = ManifestUtils.getApiKeyFromManifest(context)
                geoContext = GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(2, TimeUnit.SECONDS)
                    .writeTimeout(2, TimeUnit.SECONDS)
                    .build()

                val points = mutableListOf<LatLng>()

                // First leg: Pickup to Current
                val firstLegResult = DirectionsApi.newRequest(geoContext)
                    .origin(
                        com.google.maps.model.LatLng(
                            pickupLocation.latitude,
                            pickupLocation.longitude
                        )
                    )
                    .destination(
                        com.google.maps.model.LatLng(
                            currentLocation.latitude,
                            currentLocation.longitude
                        )
                    )
                    .mode(TravelMode.DRIVING)
                    .await()

                firstLegResult.routes.firstOrNull()?.legs?.forEach { leg ->
                    leg.steps.forEach { step ->
                        points.addAll(step.polyline.decodePath().map {
                            LatLng(it.lat, it.lng)
                        })
                    }
                }

                // Second leg: Current to Delivery
                val secondLegResult = DirectionsApi.newRequest(geoContext)
                    .origin(
                        com.google.maps.model.LatLng(
                            currentLocation.latitude,
                            currentLocation.longitude
                        )
                    )
                    .destination(
                        com.google.maps.model.LatLng(
                            deliveryLocation.latitude,
                            deliveryLocation.longitude
                        )
                    )
                    .mode(TravelMode.DRIVING)
                    .await()

                secondLegResult.routes.firstOrNull()?.legs?.forEach { leg ->
                    leg.steps.forEach { step ->
                        points.addAll(step.polyline.decodePath().map {
                            LatLng(it.lat, it.lng)
                        })
                    }
                }

                routePoints = points
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
                geoContext?.shutdown()
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        // Markers and Polyline remain the same
        Marker(
            state = MarkerState(position = pickupLocation),
            title = "Pickup Location",
            snippet = "Order starts here",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )

        Marker(
            state = MarkerState(position = currentLocation),
            title = "Current Location",
            snippet = "Driver's current position",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        )

        Marker(
            state = MarkerState(position = deliveryLocation),
            title = "Delivery Location",
            snippet = "Order will be delivered here",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        )

        if (routePoints.isNotEmpty()) {
            Polyline(
                points = routePoints,
                color = Color.Blue,
                width = 8f
            )
        }
    }
}


fun getLatLngFromAddress(
    context: Context,
    address: String,
    onResult: (LatLng?) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    geocoder.getFromLocationName(address, 1) { addresses ->
        val location = if (addresses.isNotEmpty()) {
            LatLng(addresses[0].latitude, addresses[0].longitude)
        } else null
        onResult(location)
    }
}