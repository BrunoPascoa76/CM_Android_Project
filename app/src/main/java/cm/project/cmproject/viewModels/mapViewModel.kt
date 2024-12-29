package cm.project.cmproject.viewModels

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.cmproject.models.DeliveryStatus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MapViewModel : ViewModel() {
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    fun selectLocation(location: LatLng) {
        _selectedLocation.value = location
    }

    fun listenForDeliveryStatusUpdates(deliveryId: String) {
        val database =
            FirebaseDatabase.getInstance("https://cm-android-2024-default-rtdb.europe-west1.firebasedatabase.app/")
        val deliveryStatusRef = database.getReference("deliveryStatus").child(deliveryId)

        deliveryStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deliveryStatus = snapshot.getValue(DeliveryStatus::class.java)
                deliveryStatus?.let {
                    if (it.status == "IN_TRANSIT") {
                        _userLocation.value = LatLng(it.latitude, it.longitude)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

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
}