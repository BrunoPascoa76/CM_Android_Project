package cm.project.cmproject.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.viewModels.UserViewModel

@Composable
@Preview(showBackground = true)
fun HomeScreen(modifier: Modifier = Modifier,viewModel: UserViewModel= viewModel()){
    val user = viewModel.state.collectAsState()
    Text("home")
}