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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.repositories.Result
import cm.project.cmproject.repositories.UserRepository
import cm.project.cmproject.viewModels.DeliveryViewModel
import cm.project.cmproject.viewModels.ParcelViewModel
import cm.project.cmproject.viewModels.UserViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
@Preview(showBackground = true)
fun NewOrderScreen(
    modifier: Modifier = Modifier,
    parcelViewModel: ParcelViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    deliveryViewModel: DeliveryViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
) {
    val user by userViewModel.state.collectAsStateWithLifecycle()

    var recipientEmail: String by remember { mutableStateOf("") }
    var recipientId: String? by remember { mutableStateOf(null) }

    val fromAddress by deliveryViewModel.fromAddress.collectAsState()
    val toAddress by deliveryViewModel.toAddress.collectAsState()
    val validFieldsUniversal =
        remember {
            mutableStateListOf(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
            )
        }

    var isToggled by remember { mutableStateOf(false) }

    var label: String by remember { mutableStateOf("") }
    var isFragile: Boolean by remember { mutableStateOf(false) }
    var weight: String by remember { mutableStateOf("") }
    var length: String by remember { mutableStateOf("") }
    var width: String by remember { mutableStateOf("") }
    var height: String by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit){
        deliveryViewModel.updateFromAddress(user!!.address.address) //while the user can always choose a different address if they want, it uses the account's address as a default
    }

    LaunchedEffect(recipientEmail) {
        if (recipientEmail.contains("@")) { //don't need to search unless email is valid
            when(val result = UserRepository().getUserByEmail(recipientEmail)){
                is Result.Success -> {
                    recipientId=result.data!!.uid
                    validFieldsUniversal[1]=true
                    if(toAddress.isEmpty() && result.data.address.address.isNotEmpty()){ //if the user hasn't specified a to address yet, use the recipient's registered address
                        deliveryViewModel.updateToAddress(result.data.address.address)
                        validFieldsUniversal[0]=fromAddress.isNotEmpty()
                    }
                }
                is Result.Error -> {}
            }
        }
    }

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
                                    validFieldsUniversal[0] =
                                        fromAddress.isNotEmpty() && toAddress.isNotEmpty()
                                }
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            TextButton( // Use TextButton for smaller size
                                onClick = { navController.navigate("mapScreen/fromAddress") },
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
                                onClick = { navController.navigate("mapScreen/toAddress") },
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
                InvalidFieldsMessage(validFieldsUniversal, 0, "Please enter valid addresses")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    singleLine = true,
                    label = { Text("Recipient Email") },
                    value = recipientEmail, // Display the recipient's email
                    onValueChange = {
                        recipientEmail = it; validFieldsUniversal[1] = false //for safety's sake, keep it false until we have finished verifying the email
                    }
                )
                InvalidFieldsMessage(
                    validFieldsUniversal,
                    1,
                    "Please enter a valid email"
                )
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
                    value = label,
                    onValueChange = {
                        label = it; validFieldsUniversal[2] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(
                    validFieldsUniversal,
                    2,
                    "Must enter a Description"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Weight (Kg)") },
                    value = weight,
                    onValueChange = {
                        weight = it; validFieldsUniversal[3] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 3, "Please enter the weight")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Length (cm)") },
                    value = length,
                    onValueChange = {
                        length = it; validFieldsUniversal[4] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 4, "Please enter the length")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Width (cm)") },
                    value = width,
                    onValueChange = {
                        width = it; validFieldsUniversal[5] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 5, "Please enter the width")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    label = { Text("Height (cm)") },
                    value = height,
                    onValueChange = {
                        height = it; validFieldsUniversal[6] = it.isNotEmpty()
                    }
                )
                InvalidFieldsMessage(validFieldsUniversal, 6, "Please enter the height")

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
                deliveryViewModel.createDelivery(
                    senderId = user!!.uid,
                    recipientId = recipientId!!,
                    fromAddress = fromAddress,
                    toAddress = toAddress,

                    label = label,
                    isFragile = isFragile,
                    weight = weight,
                    length = length,
                    width = width,
                    height = height,

                    context = context
                )
                navController.navigate("home") // Navigate to the home screen
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