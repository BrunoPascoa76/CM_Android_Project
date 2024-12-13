package cm.project.cmproject.components

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import cm.project.cmproject.viewModels.AddressViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.repositories.LocationRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import cm.project.cmproject.repositories.Result
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.launch
import com.google.android.libraries.places.api.model.AutocompletePrediction

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddressInput(modifier: Modifier = Modifier, addressViewModel: AddressViewModel = viewModel()) {
    var addressInput by remember { mutableStateOf("") }
    val suggestions = remember { mutableStateListOf<AutocompletePrediction>() }
    val context = LocalContext.current
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val coroutineScope = rememberCoroutineScope()

    if(!Places.isInitialized()){
        getAPIKey(context)?.let { Places.initialize(context, it) }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    when (locationPermissionState.status) {
        PermissionStatus.Granted -> {
            Column(modifier = modifier) {
                Row {
                    OutlinedTextField(
                        singleLine = true,
                        value = addressInput,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = {
                            addressInput = it
                            if (addressInput.length > 3) {
                                suggestions.clear()
                                coroutineScope.launch {
                                    when (val result = LocationRepository().getAddressSuggestions(
                                        context,
                                        addressInput
                                    )) {
                                        is Result.Success -> {
                                            suggestions.addAll(result.data)
                                        }
                                        is Result.Error -> {}
                                    }
                                }
                            }
                        },
                        label = { Text("Address") }
                    )
                    Button(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Current Location"
                        )
                    }
                }
                if (suggestions.isNotEmpty()) {
                    Column {
                        for (suggestion in suggestions) {
                            ElevatedCard(modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        when(val result=LocationRepository().getAddress(context,suggestion.placeId)){
                                            is Result.Success->{
                                                addressViewModel.setAddress(result.data)
                                                addressInput=result.data.address
                                                suggestions.clear()
                                            }
                                            is Result.Error->{
                                                suggestions.clear()
                                            }
                                        }
                                    }
                                },
                                shape=RectangleShape
                            ) {
                                Text(text = suggestion.getPrimaryText(null).toString())
                                Text(text=suggestion.getSecondaryText(null).toString(), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        else -> {
            Column(modifier = modifier) {
                Text("Location permission is required.")
                Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                }
            }
        }
    }
}

private fun getAPIKey(context: Context): String? {
    val applicationInfo = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    )
    val bundle = applicationInfo.metaData
    return bundle?.getString("com.google.android.geo.API_KEY")
}