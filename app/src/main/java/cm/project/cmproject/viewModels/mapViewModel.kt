package cm.project.cmproject.viewModels

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class MapViewModel : ViewModel() {

    // State to hold the user's location as LatLng (latitude and longitude)
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation

    // State to hold the selected place location as LatLng
    private val _selectedLocation = mutableStateOf<LatLng?>(null)
    val selectedLocation: State<LatLng?> = _selectedLocation

    // State to hold the address of the selected location
    private val _searchedText = mutableStateOf("")
    val searchedText: State<String> = _searchedText

    private val _markers = mutableStateOf<List<MarkerData>>(emptyList())
    val markers: State<List<MarkerData>> = _markers

    private lateinit var geoCoder: Geocoder

    // Define a MarkerData class to hold the position and title of a marker
    data class MarkerData(val position: LatLng, val title: String)

    fun addMarker(markerData: MarkerData) {
        _markers.value += markerData
    }

    private val _selectedLocationAddress = MutableStateFlow("")
    val selectedLocationAddress: StateFlow<String> = _selectedLocationAddress

    // Function to fetch the user's location and update the state
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                // Fetch the last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        // Update the user's location in the state
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = userLatLng
                    }
                }
            } catch (e: SecurityException) {
                Timber.e("Permission for location access was revoked: ${e.localizedMessage}")
            }
        } else {
            Timber.e("Location permission is not granted.")
        }
    }

    fun updateSelectedLocationAddress(latitude: Double, longitude: Double) {
        _selectedLocation.value = LatLng(latitude, longitude)
    }

    // Function to geocode the selected place and update the selected location state
    /*
        fun selectLocation(selectedPlace: String, context: Context) {
            viewModelScope.launch {
                val geocoder = Geocoder(context)
                val addresses = withContext(Dispatchers.IO) {
                    // Perform geocoding on a background thread
                    geocoder.getFromLocationName(selectedPlace, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    // Update the selected location in the state
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    _selectedLocation.value = latLng
                } else {
                    Timber.tag("MapScreen").e("No location found for the selected place.")
                }
            }
        }*/

    fun selectLocation(selectedPlace: String, context: Context) {
        viewModelScope.launch {
            val geocoder = Geocoder(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(selectedPlace, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: List<Address>) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            _selectedLocation.value = latLng
                        } else {
                            Timber.tag("MapScreen").e("No location found for the selected place.")
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        Timber.tag("MapScreen").e("Geocoding error: $errorMessage")
                    }
                })
            } else {
                withContext(Dispatchers.IO) {
                    try {
                        val addresses = geocoder.getFromLocationName(selectedPlace, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            withContext(Dispatchers.Main) {
                                _selectedLocation.value = latLng
                            }
                        } else {
                            Timber.tag("MapScreen").e("No location found for the selected place.")
                        }
                    } catch (e: Exception) {
                        Timber.tag("MapScreen").e("Error using geocoder: ${e.message}")
                    }
                }
            }
        }
    }

    fun getAddress(latLng: LatLng) {
        viewModelScope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                }
                _searchedText.value =
                    addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
            } catch (e: Exception) {
                Timber.e("Error fetching address: ${e.localizedMessage}")
            }
        }
    }
}