package cm.project.cmproject.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.viewModels.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AuthTabs(modifier: Modifier = Modifier) {
    val viewModel: UserViewModel = viewModel()
    val (selectedTabIndex, setSelectedTabIndex) = rememberSaveable {
        mutableIntStateOf(1)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Authenticate") },
                    modifier = Modifier.height(50.dp)
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    listOf("Login", "Register").forEachIndexed { index, title ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = { setSelectedTabIndex(index) },
                            text = { Text(text = title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> LoginScreen(modifier.padding(innerPadding), viewModel)
            1 -> RegisterScreen(modifier.padding(innerPadding), viewModel)
        }
    }

}

@Composable
fun LoginScreen(modifier: Modifier = Modifier, viewModel: UserViewModel) {
    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }
    val validFields = remember { mutableStateListOf<Boolean>(false, false) }

    ElevatedCard(modifier=modifier.padding(10.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ErrorMessage(viewModel)


            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it; validFields[0] = email.contains("@")
                }, //must be a valid email
                label = {
                    Text("Email")
                })
            InvalidFieldsMessage(validFields, 0, "Must enter a valid email")

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it; validFields[1] = password.length >= 8
                }, //must be at least 8 characters
                label = {
                    Text("Password")
                },
                visualTransformation = PasswordVisualTransformation()
            )
            InvalidFieldsMessage(validFields, 1, "Password must be at least 8 characters")


            Button(
                enabled = validFields.all { it }, //this way, you can only submit if all fields are valid
                onClick = {
                    viewModel.login(email, password)
                }, content = { Text("Login") })
        }
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, viewModel: UserViewModel) {
    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }
    var confirmPassword: String by remember { mutableStateOf("") }
    var fullName: String by remember { mutableStateOf("") }
    var phoneNumber: String by remember { mutableStateOf("") }
    var address: String by remember { mutableStateOf("") }
    var role: String by remember { mutableStateOf("user") }

    var license: String by remember { mutableStateOf("") }
    var vehicleType: String by remember { mutableStateOf("") }

    val universalValidFields =
        remember { mutableStateListOf<Boolean>(false, false, false, false, false) }
    val driverValidFields =
        remember { mutableStateListOf<Boolean>(false, false, false, false, false) }


    Column(modifier=modifier){
        ErrorMessage(viewModel)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = role == "user", onClick = { role = "user" })
                Text("User")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = role == "driver", onClick = { role = "driver" })
                Text("Driver")
            }
        }

        ElevatedCard(modifier=Modifier.padding(10.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it; universalValidFields[0] = email.contains("@")
                    }, //must be a valid email
                    label = { Text("Email") }
                )
                InvalidFieldsMessage(universalValidFields, 0, "Must enter a valid email")

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it; universalValidFields[1] =
                        password.length >= 8 && password == confirmPassword
                    }, //must be at least 8 characters
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                InvalidFieldsMessage(
                    universalValidFields,
                    1,
                    "Password must be at least 8 characters"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it; universalValidFields[1] =
                        password.length >= 8 && password == confirmPassword
                    },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                InvalidFieldsMessage(universalValidFields, 1, "Passwords must match")
            }
        }


        ElevatedCard(modifier=Modifier.padding(10.dp)) {
            Column (modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it; universalValidFields[2] = fullName.isNotEmpty()
                    },
                    label = { Text("Full Name") }
                )

                InvalidFieldsMessage(universalValidFields, 2, "Please enter your full name")
            }
        }
    }
}

@Composable
fun ErrorMessage(viewModel: UserViewModel) {
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