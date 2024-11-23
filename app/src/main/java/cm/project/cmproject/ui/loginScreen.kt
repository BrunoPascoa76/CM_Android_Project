package cm.project.cmproject.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
        mutableIntStateOf(0)
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
    var valid_fields = remember { mutableStateListOf<Boolean>(false,false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ErrorMessage(viewModel)


        OutlinedTextField(
            value = email,
            onValueChange = { email = it; valid_fields[0] = email.contains("@") }, //must be a valid email
            label = {
                Text(
                    "Email",
                    color = if (email.contains("@")) Color.Unspecified else Color.Red
                )
            })
        AnimatedVisibility(visible=!valid_fields[0]) { Text("Email must be a valid email",color=Color.Red) }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; valid_fields[1] = password.length >= 8 }, //must be at least 8 characters
            label = {
                Text(
                    "Password",
                    color = if (password.length < 8) Color.Red else Color.Unspecified
                )
            },
            visualTransformation = PasswordVisualTransformation()
        )
        AnimatedVisibility(visible=!valid_fields[1]) { Text("Password must be at least 8 characters",color=Color.Red) }


        Button(
            enabled= valid_fields.all { it }, //this way, you can only submit if all fields are valid
            onClick = {
            viewModel.login(email, password)
        }, content = { Text("Login") })
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, viewModel: UserViewModel) {
    Column(modifier = modifier) {
        Text("Register")
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
            Text(text=errorMessage!!,textAlign= TextAlign.Center,modifier=Modifier.fillMaxWidth())
        }
    }

}