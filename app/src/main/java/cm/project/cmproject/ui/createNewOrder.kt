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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import cm.project.cmproject.viewModels.ParcelViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R

@Composable
@Preview(showBackground = true)
fun NewOrderScreen(modifier: Modifier = Modifier, viewModel: ParcelViewModel = viewModel(), navController: NavHostController = rememberNavController()) {
    val parcel by viewModel.state.collectAsStateWithLifecycle()

    var parcelId: Int by remember { mutableStateOf(parcel?.parcelId ?: 0) }
    var fromAddress: String by remember { mutableStateOf(parcel?.fromAddress ?: "") }
    var toAddress: String by remember { mutableStateOf(parcel?.toAddress ?: "") }
    var toPhoneNumber: String by remember { mutableStateOf(parcel?.toPhoneNumber ?: "") }
    var toEmail: String by remember { mutableStateOf(parcel?.toEmail ?: "") }

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
                        value = toEmail,
                        onValueChange = {
                            toEmail = it; validFieldsUniversal[2] = toEmail.contains("@")
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
                        value = toPhoneNumber,
                        onValueChange = {
                            toPhoneNumber = it; validFieldsUniversal[3] = toPhoneNumber.isNotEmpty()
                        }
                    )
                    InvalidFieldsMessage(validFieldsUniversal, 3, "Please enter your phone number")


                        ElevatedButton(
                            enabled = validFieldsUniversal.all { it },
                            onClick = {
                                viewModel.register(
                                    fromAddress = fromAddress,
                                    toAddress = toAddress,
                                    toEmail = toEmail,
                                    toPhoneNumber = toPhoneNumber
                                )
                            }
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