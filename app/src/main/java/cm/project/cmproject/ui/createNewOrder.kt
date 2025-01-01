package cm.project.cmproject.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.viewModels.DeliveryViewModel
import cm.project.cmproject.viewModels.ParcelViewModel
import cm.project.cmproject.viewModels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
@Preview(showBackground = true)
fun NewOrderScreen(
    modifier: Modifier = Modifier,
    parcelViewModel: ParcelViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    deliveryViewModel: DeliveryViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val parcel by parcelViewModel.state.collectAsStateWithLifecycle()
    val delivery by deliveryViewModel.state.collectAsStateWithLifecycle()
    val user by userViewModel.state.collectAsStateWithLifecycle()

    var parcelId: String by remember { mutableStateOf("") } // Initialize as empty string
    var deliveryId: String by remember { mutableStateOf("") } // Initialize as empty string
    var email: String by remember { mutableStateOf(user?.email ?: "") }
    var userlId: String by remember { mutableStateOf(user?.uid ?: "") }
    val fromAddress by deliveryViewModel.fromAddress.collectAsState()
    val toAddress by deliveryViewModel.toAddress.collectAsState()
    var phoneNumber: String by remember { mutableStateOf(user?.phoneNumber ?: "") }
    val validFieldsUniversal =
        remember { mutableStateListOf(true, true, true, true, true, true, true, true, true) }

    var isToggled by remember { mutableStateOf(false) }

    var label: String by remember { mutableStateOf("") }
    var isFragile: Boolean by remember { mutableStateOf(false) }
    var weight: String by remember { mutableStateOf("") }
    var length: String by remember { mutableStateOf("") }
    var width: String by remember { mutableStateOf("") }
    var height: String by remember { mutableStateOf("") }


    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "New Order Details", fontSize = 25.sp)

        ElevatedCard {
            Text(text = "Delivery", fontSize = 20.sp, modifier = Modifier.padding(10.dp))
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Toggle Button with larger size and padding
                    IconButton(
                        onClick = { isToggled = !isToggled },
                        modifier = Modifier
                            .background(if (isToggled) Color.Green else Color.LightGray)
                            .clip(CircleShape)
                            .padding(4.dp)
                    )
                    {
                        Text(text = if (isToggled) "↑↓" else "↓↑", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                modifier = Modifier.weight(1f), // Occupy remaining space
                                label = { Text("From Address") },
                                value = if (isToggled) deliveryViewModel.toAddress.value else deliveryViewModel.fromAddress.value,
                                onValueChange = { newValue: String ->
                                    if (isToggled) {
                                        deliveryViewModel.updateToAddress(newValue)
                                    } else {
                                        deliveryViewModel.updateFromAddress(newValue)
                                    }
                                    validFieldsUniversal[0] = toAddress.isNotEmpty()
                                }
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            TextButton( // Use TextButton for smaller size
                                onClick = { navController.navigate("mapScreen?addressType=fromAddress") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .shadow(
                                        4.dp,
                                        shape = RectangleShape,
                                        clip = false,
                                        spotColor = Color.LightGray
                                    )
                                    .background(Color.LightGray)
                                    .width(90.dp)
                                    .height(40.dp)
                            ) {
                                Text(text = "Select", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically, // Align items vertically to center
                            horizontalArrangement = Arrangement.SpaceBetween // Add space between items
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                modifier = Modifier.weight(1f), // Occupy remaining space
                                label = { Text("To Address") },
                                value = if (isToggled) deliveryViewModel.fromAddress.value else deliveryViewModel.toAddress.value,
                                onValueChange = { newValue: String ->
                                    if (isToggled) {
                                        deliveryViewModel.updateFromAddress(newValue)
                                    } else {
                                        deliveryViewModel.updateToAddress(newValue)
                                    }
                                    validFieldsUniversal[1] = fromAddress.isNotEmpty()
                                }
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            TextButton( // Use TextButton for smaller size
                                onClick = { navController.navigate("mapScreen?addressType=toAddress") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .shadow(
                                        4.dp,
                                        shape = RectangleShape,
                                        clip = false,
                                        spotColor = Color.LightGray
                                    )
                                    .background(Color.LightGray)
                                    .width(90.dp)
                                    .height(40.dp)
                            ) {
                                Text(text = "Select", fontSize = 10.sp)
                            }
                        }
                    }
                }
                //InvalidFieldsMessage(validFieldsUniversal, 1, "Please enter your full name")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    singleLine = true,
                    label = { Text("Customer Email") },
                    value = email, // Display the current user's email
                    onValueChange = {
                        email = it; validFieldsUniversal[2] = it.contains("@")
                    }
                )
                InvalidFieldsMessage(
                    validFieldsUniversal,
                    0,
                    "Must enter a valid email"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Phone Number") },
                    value = phoneNumber, // Display the current user's phone number
                    onValueChange = {
                        phoneNumber = it; validFieldsUniversal[3] = phoneNumber.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 3, "Please enter your phone number")

            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ElevatedCard {
            Text(text = "Parcel", fontSize = 20.sp, modifier = Modifier.padding(10.dp))
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    singleLine = true,
                    label = { Text("Description") },
                    value = label, // Display the current user's email
                    onValueChange = {
                        label = it; validFieldsUniversal[4] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(
                    validFieldsUniversal,
                    4,
                    "Must enter a Description"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Weight Kg") },
                    value = "0.000 Kg", // Display the current user's phone number
                    onValueChange = {
                        weight = it; validFieldsUniversal[5] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 5, "Please enter the weight")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Length cm") },
                    value = "0 cm", // Display the current user's phone number
                    onValueChange = {
                        length = it; validFieldsUniversal[6] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 6, "Please enter the length")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Width cm") },
                    value = "0 cm", // Display the current user's phone number
                    onValueChange = {
                        width = it; validFieldsUniversal[7] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 7, "Please enter the width")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Height cm") },
                    value = "0 cm", // Display the current user's phone number
                    onValueChange = {
                        height = it; validFieldsUniversal[8] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 8, "Please enter the height")

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Fragile"
                    )
                    Checkbox(
                        checked = isFragile,
                        onCheckedChange = { isFragile = it }
                    )
                }
            }
        }
        ElevatedButton(
            enabled = validFieldsUniversal.all { it },
            onClick = {
                // Generate a UUID for parcelId
                parcelId = java.util.UUID.randomUUID().toString()
                deliveryId = java.util.UUID.randomUUID().toString()
                coroutineScope.launch {
                    deliveryViewModel.createDelivery(
                        deliveryId = deliveryId,
                        parcelId = parcelId,
                        userId = userlId,
                        fromAddress = fromAddress,
                        toAddress = toAddress,
                        email = email,
                        phoneNumber = phoneNumber,
                        label = label,
                        isFragile = isFragile,
                        weight = weight,
                        length = length,
                        width = width,
                        height = height,
                    )
                    navController.navigate("home") // Navigate to the home screen
                }
            },
        ) {
            Text("Submit")
        }
    }
}

@Composable
private fun ColumnScope.InvalidFieldsMessage(
    validFields: SnapshotStateList<Boolean>,
    index: Int,
    message: String
) {
    AnimatedVisibility(visible = !validFields[index]) {
        Text(
            message,
            color = Color.Red
        )
    }
}