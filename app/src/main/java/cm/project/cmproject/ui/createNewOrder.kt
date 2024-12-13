package cm.project.cmproject.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.viewModels.ParcelViewModel
import cm.project.cmproject.viewModels.UserViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.viewModels.DeliveryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
@Preview(showBackground = true)
fun NewOrderScreen(modifier: Modifier = Modifier, parcelViewModel: ParcelViewModel = viewModel(), userViewModel: UserViewModel = viewModel(), deliveryViewModel: DeliveryViewModel = viewModel(), navController: NavHostController = rememberNavController(), coroutineScope: CoroutineScope = rememberCoroutineScope()) {
    val parcel by parcelViewModel.state.collectAsStateWithLifecycle()
    val delivery by deliveryViewModel.state.collectAsStateWithLifecycle()
    val user by userViewModel.state.collectAsStateWithLifecycle()

    var parcelId: Int by remember { mutableStateOf(parcel?.parcelId?: 0) }
    var userlId: String by remember { mutableStateOf(user?.uid?: "") }
    var fromAddress: String by remember { mutableStateOf(delivery?.fromAddress ?: "") }
    var toAddress: String by remember { mutableStateOf(delivery?.toAddress ?: "") }
    var phoneNumber: String by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var email: String by remember { mutableStateOf(user?.email ?: "") }

    val validFieldsUniversal = remember { mutableStateListOf(true,true,true,true) }

    Column(
        modifier = modifier.padding(horizontal = 10.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "New Order Details", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(10.dp))

            ElevatedCard {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedTextField(
                        singleLine = true,
                        label = { Text("Address") },
                        value = fromAddress,
                        onValueChange = {
                            fromAddress = it; validFieldsUniversal[1] = toAddress.isNotEmpty()
                        },
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        singleLine = true,
                        label = { Text("Address") },
                        value = toAddress,
                        onValueChange = {
                            toAddress = it; validFieldsUniversal[1] = toAddress.isNotEmpty()
                        },
                    )
                    //InvalidFieldsMessage(validFieldsUniversal, 1, "Please enter your full name")

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        singleLine = true,
                        label = { Text("Email") },
                        value = email,
                        onValueChange = {
                            email = it; validFieldsUniversal[2] = email.contains("@")
                        }
                    )
                    InvalidFieldsMessage(
                        validFieldsUniversal,
                        2,
                        "Must enter a valid email"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        label = { Text("Phone Number") },
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it; validFieldsUniversal[3] = phoneNumber.isNotEmpty()
                        }
                    )
                    InvalidFieldsMessage(validFieldsUniversal, 3, "Please enter your phone number")


                        ElevatedButton(
                            enabled = validFieldsUniversal.all { it },
                            onClick = {
                                coroutineScope.launch {
                                    deliveryViewModel.createDelivery(
                                        userId = userlId,
                                        fromAddress = fromAddress,
                                        toAddress = toAddress,
                                        toEmail = email,
                                        toPhoneNumber = phoneNumber
                                    )
                                    navController.navigate("home") // Navigate to the home screen
                                }
                            },
                        ) {
                            Text("Submit")
                        }

                }
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