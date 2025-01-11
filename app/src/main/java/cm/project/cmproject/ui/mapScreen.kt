package cm.project.cmproject.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import cm.project.cmproject.components.SearchBar
import cm.project.cmproject.utils.ManifestUtils
import cm.project.cmproject.viewModels.DeliveryViewModel
import cm.project.cmproject.viewModels.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import timber.log.Timber


@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    deliveryViewModel: DeliveryViewModel,
    navController: NavHostController,
    addressType: String
) {
    // Obtain the current context
    val context = LocalContext.current

    // Retrieve the API key from the manifest file
    val apiKey = ManifestUtils.getApiKeyFromManifest(context)
    // Initialize the Places API with the retrieved API key
    if (!Places.isInitialized() && apiKey != null) {
        Places.initialize(context, apiKey)
    }

    val clickedLocation = remember { mutableStateOf<LatLng?>(null) } // Tracks map click
    // Initialize the camera position state, which controls the camera's position on the map
    val cameraPositionState = rememberCameraPositionState()

    // Observe the user's location from the ViewModel
    val userLocation by mapViewModel.userLocation
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Observe the selected location from the ViewModel
    val selectedLocation by mapViewModel.selectedLocation

    val markers = mapViewModel.markers

    // Handle permission requests for accessing fine location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Fetch the user's location and update the camera if permission is granted
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
        } else {
            // Handle the case when permission is denied
            Timber.e("Location permission was denied by the user.")
        }
    }
    val updateAddress: (String) -> Unit = {
        if (addressType == "fromAddress") {
            deliveryViewModel.updateFromAddress(it)
        } else if (addressType == "toAddress") {
            deliveryViewModel.updateToAddress(it)
        }
    }

    // Request the location permission when the composable is launched
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            // Check if the location permission is already granted
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Fetch the user's location and update the camera
                mapViewModel.fetchUserLocation(context, fusedLocationClient)
            }

            else -> {
                // Request the location permission if it has not been granted
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    /*
        LaunchedEffect(cameraPositionState.isMoving) {
            if (!cameraPositionState.isMoving) {
                mapViewModel.getAddress(cameraPositionState.position.target)
            }
        }
    */
    // Layout that includes the search bar and the map, arranged in a vertical column
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(18.dp)) // Add a spacer with a height of 18dp to push the search bar down

        // Add the search bar component
        SearchBar(
            onPlaceSelected = { place ->
                // When a place is selected from the search bar, update the selected location
                mapViewModel.selectLocation(place, context)
                updateAddress(place)
            },
            modifier = Modifier
                .padding(16.dp)
                .align(
                    alignment = Alignment.CenterHorizontally
                )
        )
        Button(
            onClick = {
                val selectedAddress = mapViewModel.selectedLocationAddress.value
                if (selectedAddress.isNotEmpty()) {
                    updateAddress(selectedAddress) // Pass to the ViewModel for syncing
                }
                navController.navigate("createneworder")
            },
            modifier = Modifier
                .padding(16.dp)
                .align(
                    alignment = Alignment.CenterHorizontally
                ) // Position at the bottom center
        ) {
            Text("Return to Order")
        }

        // Display the Google Map
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Adjust padding to avoid overlap
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                clickedLocation.value = latLng // Update clicked location
                Timber.d("Map clicked at: $latLng")
            }
        ) {
            // If the user's location is available, place a marker on the map
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it), // Place the marker at the user's location
                    title = "Your Location", // Set the title for the marker
                    snippet = "This is where you are currently located." // Set the snippet for the marker
                )
                // Move the camera to the user's location with a zoom level of 10f
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 10f)
            }

            // If a location was selected from the search bar, place a marker there
            selectedLocation?.let {
                mapViewModel.updateSelectedLocation(it.latitude, it.longitude)
                Marker(
                    state = MarkerState(position = it), // Place the marker at the selected location
                    title = "Selected Location", // Set the title for the marker
                    snippet = "This is the place you selected." // Set the snippet for the marker
                )
                // Move the camera to the selected location with a zoom level of 15f
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
            }

            clickedLocation.value?.let {
                selectedLocation = LatLng(it.latitude, it.longitude)
                Marker(
                    state = MarkerState(position = it),
                    title = "Clicked Location",
                    snippet = "Lat: ${it.latitude}, Lng: ${it.longitude}"
                )
            }
        }
    }

}
