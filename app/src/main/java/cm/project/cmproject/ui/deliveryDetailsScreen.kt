package cm.project.cmproject.ui

import android.graphics.Bitmap
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R
import cm.project.cmproject.components.DeliveryProgressBar
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.viewModels.DeliveryViewModel
import cm.project.cmproject.viewModels.UserViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun DeliveryDetailsScreen(
    modifier: Modifier = Modifier,
    deliveryId: String = "123",
    navController: NavController = rememberNavController(),
    deliveryViewModel: DeliveryViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    deliveryViewModel.fetchDelivery(deliveryId)
    val delivery by deliveryViewModel.state.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier, topBar = {
        CenterAlignedTopAppBar(title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text("Delivery Details")
                }
            }
        })
    }) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ErrorMessage(deliveryViewModel)
            if (delivery == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            } else {
                OrderDetails(
                    deliveryViewModel = deliveryViewModel,
                    userViewModel = userViewModel,
                    navController = navController
                )

            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OrderDetails(
    modifier: Modifier = Modifier,
    deliveryViewModel: DeliveryViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val delivery by deliveryViewModel.state.collectAsState()
    val recipient by deliveryViewModel.recipient.collectAsState()
    val sender by deliveryViewModel.sender.collectAsState()
    val user by userViewModel.state.collectAsState()

    Column(
        modifier = modifier
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ElevatedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Parcel Details", fontWeight = FontWeight.Bold)
                DetailsRow("Title", delivery?.parcel?.label ?: "N/A")
                DetailsRow("Status", delivery?.status ?: "Unknown")


                if (recipient != null) {
                    HorizontalDivider(thickness = 1.dp)
                    Text("Recipient Details", fontWeight = FontWeight.Bold)
                    DetailsRow("Name", recipient!!.fullName)
                    DetailsRow("Phone Number", recipient!!.phoneNumber)
                    DetailsRow("Address", recipient!!.address.address)
                }

                if (sender != null) {
                    HorizontalDivider(thickness = 1.dp)
                    Text("Sender Details", fontWeight = FontWeight.Bold)
                    DetailsRow("Name", sender!!.fullName)
                    DetailsRow("Phone Number", sender!!.phoneNumber)
                    DetailsRow("Address", sender!!.address.address)
                }
            }

        }
        Column(
            modifier = Modifier.padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DeliveryProgressBar(deliveryViewModel = deliveryViewModel)
            if (delivery!!.status != "delivered") { //no need to show any of these for past deliveries
                if (user != null && user!!.role == "customer") { //if customer, display qr codes
                    QrCode(delivery!!.deliveryId)
                } else { //if driver, let them edit the progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            10.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        if (delivery!!.status != "Delivered" && user!!.role == "driver") {
                            ElevatedButton(
                                onClick = { navController.navigate("addDeliveryStep") },
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Step"
                                    )
                                    Text("Add Step")
                                }
                            }
                        }


                        if (delivery!!.completedSteps == delivery!!.steps.size - 1) { //the last step must be completed using the qr code (the rest can be completed by just clicking the button
                            ProximityCheckService(delivery!!, navController)
                        } else { //the others just need a click of a button
                            ElevatedButton(onClick = {
                                deliveryViewModel.completeCurrentStep()
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Complete Step"
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Complete Step")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanQRButton(navController: NavController, isWithinBounds: Boolean) {
    ElevatedButton(
//        enabled = isWithinBounds,
        onClick = { navController.navigate("qrCodeScanner") }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.qr_code_scanner_24px),
                contentDescription = "Scan QR Code"
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text("Enter qr code")
        }
    }
}

@Composable
private fun QrCode(deliveryId: String) {
    val bitmap = generateQrCode(deliveryId)
    if (bitmap != null) {
        Box(modifier = Modifier.background(Color.White)) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun DetailsRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "${title}:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f)
        )
        Text(
            value, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun ErrorMessage(viewModel: DeliveryViewModel) {
    val errorMessage: String? by viewModel.errorMessage.collectAsState()
    if (errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = errorMessage!!,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

//Everyone just draws it pixel by pixel
private fun generateQrCode(deliveryId: String, size: Int = 200): Bitmap? {
    return try {
        val bitMatrix = MultiFormatWriter().encode(deliveryId, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.TRANSPARENT
                )
            }
        }
        bitmap
    } catch (e: WriterException) {
        null
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProximityCheckService(
    delivery: Delivery,
    navController: NavController
) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(LocalContext.current)
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var currentLocation: Location? = null
    var locationCallback: LocationCallback? = null

    var isWithinBounds by remember { mutableStateOf(false) }


    DisposableEffect(Unit) {
        //setup request and callback
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMinUpdateIntervalMillis(15000)
                .build()


        //callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.lastLocation
                val targetAddress = delivery.steps[delivery.completedSteps].location

                if (targetAddress.address == "") {
                    isWithinBounds = true
                } else if (currentLocation != null) {
                    val location1 = Location("a")
                    location1.latitude = currentLocation!!.latitude
                    location1.longitude = currentLocation!!.longitude

                    val location2 = Location("b")
                    location2.latitude = targetAddress.latitude
                    location2.longitude = targetAddress.longitude


                    isWithinBounds = location1.distanceTo(location2) <= 5000
                } else {
                    isWithinBounds = false
                }
            }
        }


        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        } else {
            //launch location tracking
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                null
            )
        }

        onDispose {
            //stop tracking location
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        }
    }
    ScanQRButton(navController, isWithinBounds)
}
