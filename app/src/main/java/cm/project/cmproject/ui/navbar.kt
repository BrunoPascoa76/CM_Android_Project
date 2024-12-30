package cm.project.cmproject.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cm.project.cmproject.R
import cm.project.cmproject.viewModels.UserViewModel

@Composable
fun Navbar(
    navController: NavHostController,
    userViewModel: UserViewModel,
    content: @Composable () -> Unit
) {

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val user by userViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "order"
                        )
                    },
                    label = { Text("Order") },
                    selected = currentRoute == "order",
                    onClick = { navController.navigate("order") }
                )
                NavigationBarItem(
                    modifier = Modifier.padding(bottom = 0.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    selected = currentRoute == "profile",
                    onClick = { navController.navigate("profile") }
                )
                if (user?.role == "driver") {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.local_shipping_24px),
                                contentDescription = "Lobby"
                            )
                        },
                        label = { Text("Lobby") },
                        selected = currentRoute == "lobby",
                        onClick = { navController.navigate("lobby") }
                    )

                }
            }
        },

        ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}