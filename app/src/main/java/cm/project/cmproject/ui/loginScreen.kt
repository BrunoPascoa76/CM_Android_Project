package cm.project.cmproject.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.viewModels.UserViewModel

@Preview
@Composable
fun LoginScreen(modifier: Modifier=Modifier){
    val viewModel: UserViewModel =viewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Column(modifier = modifier) {
        Text(text = "Login Screen")

    }
}