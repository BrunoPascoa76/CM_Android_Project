package cm.project.cmproject.components

import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cm.project.cmproject.viewModels.AddressViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddressInput(modifier: Modifier = Modifier, addressViewModel: AddressViewModel=viewModel()){
    var addressInput by remember { mutableStateOf("") }
    val suggestions = remember { mutableListOf<Address>() }
    val geocoder = Geocoder(LocalContext.current, Locale.getDefault())
    val locationPermissionState=rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    when(locationPermissionState.status) {
        PermissionStatus.Granted -> {
            Column(modifier=modifier) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = addressInput,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = {
                            if (addressInput.length > 3) {
                                geocoder.getFromLocationName(addressInput, 5) { addresses ->
                                    if (addresses.isNotEmpty()) {
                                        addressViewModel.setAddress(addresses[0])
                                        suggestions.clear()
                                        suggestions.addAll(addresses)
                                    } else {
                                        addressViewModel.clearAddress()
                                        suggestions.clear()
                                    }
                                }
                            }
                        },
                        label = { Text("Address") }
                    )
                    Button(onClick = {}){
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Current Location"
                        )
                    }
                }
                if (suggestions.isNotEmpty()){
                    LazyColumn(modifier=Modifier.fillMaxWidth()) {
                        items(suggestions.size) { index ->
                            val suggestion = suggestions[index]
                            Card(modifier=Modifier.fillMaxWidth().clickable{
                                addressInput=suggestion.getAddressLine(0)?:addressInput
                            }) {
                                Text(text = suggestion.getAddressLine(0))
                            }
                        }
                    }
                }
            }
        }
        else -> {
            Column(modifier=modifier) {
                Text("Location permission is required.")
                Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                }
            }
        }
    }
}