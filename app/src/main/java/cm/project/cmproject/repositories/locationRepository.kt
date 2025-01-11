package cm.project.cmproject.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import cm.project.cmproject.models.Address
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.tasks.await

class LocationRepository {
    suspend fun getAddressSuggestions(
        context: Context,
        address: String,
        maxPredictions: Int = 5
    ): Result<List<AutocompletePrediction>> {
        return try {
            val placesClient = Places.createClient(context)
            val autocompleteRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery(address)
                .build()
            val autocompleteResponse =
                placesClient.findAutocompletePredictions(autocompleteRequest).await()
            return Result.Success(autocompleteResponse.autocompletePredictions.take(maxPredictions))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAddress(context: Context, placeId: String): Result<Address> {
        return try {
            val placesClient = Places.createClient(context)
            val autocompleteRequest = FetchPlaceRequest.newInstance(
                placeId, listOf(
                    Place.Field.FORMATTED_ADDRESS,
                    Place.Field.ADDRESS_COMPONENTS,
                    Place.Field.LOCATION,
                    Place.Field.PLUS_CODE
                )
            )
            val response = placesClient.fetchPlace(autocompleteRequest).await()
            Result.Success(createAddressFromComponents(response.place))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun createAddressFromComponents(place: Place): Address {
        val address = Address(
            address = place.formattedAddress ?: "",
            latitude = place.location?.latitude ?: 0.0,
            longitude = place.location?.longitude ?: 0.0
        )

        return address
    }

    @SuppressLint("MissingPermission") //it seems it didn't recognize the "if" at the start as "checking for permission"
    @OptIn(ExperimentalPermissionsApi::class)
    suspend fun getCurrentLocation(
        context: Context,
        permissionState: PermissionState
    ): Result<Address> {
        if (!permissionState.status.isGranted) {
            return Result.Error(Exception("Location permission not granted"))
        }
        return try {
            val placesClient = Places.createClient(context)
            val request = FindCurrentPlaceRequest.newInstance(
                listOf(
                    Place.Field.FORMATTED_ADDRESS,
                    Place.Field.LOCATION,
                    Place.Field.PLUS_CODE
                )
            )
            val response = placesClient.findCurrentPlace(request).await()

            if (response.placeLikelihoods.isEmpty()) {
                Result.Error(Exception("No place found"))
            }
            Result.Success(createAddressFromComponents(response.placeLikelihoods[0].place))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    @Suppress("DEPRECATION")//had to suppress the deprecation warning as the non-deprecated version only works with API>=33, which requires very recent phones
    fun getCoordinatesFromAddress(context: Context, address: String): Result<Address> {
        return try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocationName(
                address,
                1
            ) //geocoder is better for this specific case (in terms of code amount)
            if (!addresses.isNullOrEmpty()) {
                Result.Success(
                    Address(
                        address = addresses[0].getAddressLine(0),
                        latitude = addresses[0].latitude,
                        longitude = addresses[0].longitude
                    )
                )
            } else {
                Result.Error(Exception("No address found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    @Suppress("DEPRECATION")
    suspend fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Result<Address> {
        return try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                Result.Success(
                    Address(
                        address = addresses[0].getAddressLine(0),
                        latitude = addresses[0].latitude,
                        longitude = addresses[0].longitude
                    )
                )
            } else {
                Result.Error(Exception("No address found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}