package cm.project.cmproject.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.viewModels.UserViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R
import cm.project.cmproject.components.AddressInput
import cm.project.cmproject.viewModels.AddressViewModel

@Composable
@Preview(showBackground = true)
fun ProfileScreen(modifier: Modifier = Modifier, userViewModel: UserViewModel = viewModel(), addressViewModel: AddressViewModel=viewModel(), navController: NavHostController = rememberNavController()) {
    val user by userViewModel.state.collectAsStateWithLifecycle()
    val address by addressViewModel.state.collectAsState()

    if(address==null && user?.address!=null){
        addressViewModel.setAddress(user?.address)
    }


    var email: String by remember { mutableStateOf(user?.email ?: "") }
    var fullName: String by remember { mutableStateOf(user?.fullName ?: "") }
    var phoneNumber: String by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var license: String by remember { mutableStateOf(user?.license ?: "") }
    var vehicleType: String by remember { mutableStateOf(user?.vehicleType ?: "") }

    val validFieldsUniversal = remember { mutableStateListOf(true,true,true,true) }
    val validFieldsDriver = remember { mutableStateListOf(true,true) }

    var isEditing by remember { mutableStateOf(false) }

    validFieldsUniversal[3]= address!=null

    Column(
        modifier = modifier.padding(horizontal = 10.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(
                onClick = { isEditing = !isEditing },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (isEditing) Color.Red else Color.Green
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isEditing) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        Text("Edit")
                    } else {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Cancel")
                        Text("Cancel")
                    }
                }
            }
        }

        Text(text = if (isEditing) "Edit profile" else "Profile details", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(10.dp))

        if (!isEditing) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailsRow(
                        label = "Email:",
                        icon = Icons.Default.Email,
                        value = user?.email ?: "N/A"
                    )
                    DetailsRow(
                        label = "Full Name:",
                        icon = Icons.Default.Person,
                        value = user?.fullName ?: "N/A"
                    )
                    DetailsRow(
                        label = "Phone Number:",
                        icon = Icons.Default.Phone,
                        value = user?.phoneNumber ?: "N/A"
                    )
                    DetailsRow(
                        label = "Address:",
                        icon = Icons.Default.Place,
                        value = user?.address?.address ?: "N/A"
                    )
                    if (user?.role == "driver") {
                        DetailsRow(
                            label = "License:",
                            icon = ImageVector.vectorResource(id = R.drawable.id_card_24px),
                            value = user?.license ?: "N/A"
                        )
                        DetailsRow(
                            label = "Vehicle Type:",
                            icon = ImageVector.vectorResource(id = R.drawable.local_shipping_24px),
                            value = user?.vehicleType ?: "N/A"
                        )
                    }
                }
            }
        } else {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedTextField(
                        singleLine = true,
                        label = { Text("Email") },
                        value = email,
                        onValueChange = {
                            email = it; validFieldsUniversal[0] = email.contains("@")
                        },
                    )
                    InvalidFieldsMessage(validFieldsUniversal, 0, "Must enter a valid email")

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        singleLine = true,
                        label = { Text("Full Name") },
                        value = fullName,
                        onValueChange = {
                            fullName = it; validFieldsUniversal[1] = fullName.isNotEmpty()
                        },
                    )
                    InvalidFieldsMessage(validFieldsUniversal, 1, "Please enter your full name")

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        singleLine = true,
                        label = { Text("Phone Number") },
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it; validFieldsUniversal[2] = phoneNumber.isNotEmpty()
                        }
                    )
                    InvalidFieldsMessage(
                        validFieldsUniversal,
                        2,
                        "Please enter your phone number"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AddressInput(addressViewModel = addressViewModel,modifier=Modifier.fillMaxWidth(0.7f))
                    InvalidFieldsMessage(validFieldsUniversal, 3, "Please enter your address")

                    if (user?.role == "driver") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            singleLine = true,
                            label = { Text("License") },
                            value = license,
                            onValueChange = {
                                license = it; validFieldsDriver[0] = license.isNotEmpty()
                            }
                        )
                        InvalidFieldsMessage(validFieldsDriver, 0, "Please enter your license")

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            singleLine = true,
                            label = { Text("Vehicle Type") },
                            value = vehicleType,
                            onValueChange = {
                                vehicleType = it; validFieldsDriver[1] = vehicleType.isNotEmpty()
                            }
                        )
                        InvalidFieldsMessage(validFieldsDriver, 1, "Please enter your vehicle type")

                        Spacer(modifier = Modifier.height(10.dp))
                        ElevatedButton(
                            colors = ButtonDefaults.elevatedButtonColors().copy(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
                            enabled = validFieldsUniversal.all { it } && validFieldsDriver.all { it },
                            onClick = {
                                userViewModel.update(
                                    user!!.copy(
                                        email = email,
                                        fullName = fullName,
                                        phoneNumber = phoneNumber,
                                        address = address!!,
                                        license = license,
                                        vehicleType = vehicleType
                                    )
                                )
                                isEditing = false
                            }
                        ) {
                            Text("Update")
                        }
                    } else {
                        ElevatedButton(
                            enabled = validFieldsUniversal.all { it },
                            onClick = {
                                userViewModel.update(
                                    user!!.copy(
                                        email = email,
                                        fullName = fullName,
                                        phoneNumber = phoneNumber,
                                        address = address!!
                                    )
                                )
                                isEditing = false
                            }
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
        }

        ElevatedButton(
            enabled = validFieldsUniversal.all { it } && validFieldsDriver.all { it },
            onClick = {
                userViewModel.logout()
                navController.navigate("auth")
            }
        ) {
            Text("Logout")
        }
    }
}


@Composable
fun DetailsRow(modifier: Modifier = Modifier, label: String, icon: ImageVector, value: String) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.padding(end = 5.dp)
            )
            Text(label)
        }
        Text(value)
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