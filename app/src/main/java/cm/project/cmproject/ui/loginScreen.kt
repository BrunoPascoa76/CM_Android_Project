package cm.project.cmproject.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
            Column() {
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
    Column(modifier = modifier) {
        Text("Login")
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, viewModel: UserViewModel) {
    Column(modifier = modifier) {
        Text("Register")
    }
}