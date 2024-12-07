package cm.project.cmproject.ui.navigation

import OrderViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.ui.AuthTabs
import cm.project.cmproject.ui.HomeScreen
import cm.project.cmproject.ui.Navbar
import cm.project.cmproject.ui.OrderScreen
import cm.project.cmproject.ui.ProfileScreen
import cm.project.cmproject.viewModels.UserViewModel
import kotlinx.serialization.Serializable


/**
 * Provides Navigation graph for the application.
 */

// Routes
@Serializable
object Home
object Profile
object Order

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "auth"
) {
    val viewModel: UserViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("auth") {
            AuthTabs(navController = navController, viewModel = viewModel)
        }
        composable<Home> {
            Navbar(navController) {
                HomeScreen(viewModel=viewModel)
            }
        }
        composable<Profile> {
            Navbar(navController) {
                ProfileScreen(viewModel=viewModel, navController = navController)
            }
        }
        composable<Order> {
            Navbar(navController) {
                OrderScreen(viewModel=orderViewModel)
            }
        }
    }
}