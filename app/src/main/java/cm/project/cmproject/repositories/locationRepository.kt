package cm.project.cmproject.repositories

import android.annotation.SuppressLint
import android.content.Context
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
}