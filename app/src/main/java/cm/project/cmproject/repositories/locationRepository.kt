package cm.project.cmproject.repositories

import android.content.Context
import android.location.Address
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationRepository{
    suspend fun getAddressSuggestions(context: Context, address:String,maxPredictions:Int=5): Result<List<AutocompletePrediction>>{
        return try {
            val placesClient = Places.createClient(context)
            val autocompleteRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery(address)
                .build()
            val autocompleteResponse=placesClient.findAutocompletePredictions(autocompleteRequest).await()
            return Result.Success(autocompleteResponse.autocompletePredictions.take(maxPredictions))
        }catch (e:Exception){
            Result.Error(e)
        }
    }

    suspend fun getAddress(context: Context,placeId:String): Result<Address>{
        return try{
            val placesClient = Places.createClient(context)
            val autocompleteRequest = FetchPlaceRequest.newInstance(placeId,listOf(
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LOCATION,
                Place.Field.PLUS_CODE
            ))
            val response=placesClient.fetchPlace(autocompleteRequest).await()
            Result.Success(createAddressFromComponents(response.place))
        }catch (e:Exception){
            Result.Error(e)
        }
    }

    private fun createAddressFromComponents(place:Place): Address{
        val address=Address(Locale.getDefault())
        address.latitude=place.location?.latitude?:0.0
        address.longitude=place.location?.longitude?:0.0

        place.addressComponents?.asList()?.forEach { component ->
            when {
                component.types.contains("street_number") -> address.subThoroughfare = component.name
                component.types.contains("route") -> address.thoroughfare = component.name
                component.types.contains("locality") -> address.locality = component.name
                component.types.contains("administrative_area_level_1") -> address.adminArea = component.name
                component.types.contains("country") -> address.countryName = component.name
                component.types.contains("postal_code") -> address.postalCode = component.name
            }
        }
        address.featureName=place.plusCode?.globalCode

        return address
    }
}