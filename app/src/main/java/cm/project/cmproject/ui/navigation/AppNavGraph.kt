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
import cm.project.cmproject.ui.DeliveryDetailsScreen
import cm.project.cmproject.ui.HomeScreen
import cm.project.cmproject.ui.Navbar
import cm.project.cmproject.ui.OrderScreen
import cm.project.cmproject.ui.ProfileScreen
import cm.project.cmproject.ui.QrCodeScannerScreen
import cm.project.cmproject.viewModels.DeliveryViewModel
import cm.project.cmproject.viewModels.UserViewModel


/**
 * Provides Navigation graph for the application.
 */

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "auth"
) {
    val userViewModel: UserViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val deliveryViewModel: DeliveryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("auth") {
            AuthTabs(navController = navController, viewModel = userViewModel)
        }
        composable("home") {
            Navbar(navController) {
                HomeScreen(viewModel=userViewModel)
            }
        }
        composable("profile") {
            Navbar(navController) {
                ProfileScreen(viewModel=userViewModel, navController = navController)
            }
        }
        composable("order") {
            Navbar(navController) {
                OrderScreen(viewModel=orderViewModel)
            }
        }
        composable("deliveryDetails/{deliveryId}"){ backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getString("deliveryId")?.toIntOrNull()?:0
            DeliveryDetailsScreen(
                deliveryId = deliveryId,
                navController = navController,
                deliveryViewModel = deliveryViewModel,
                userViewModel = userViewModel
            )
        }
        composable("qrCodeScanner"){
            QrCodeScannerScreen(navController=navController, viewModel = deliveryViewModel)
        }
    }
}