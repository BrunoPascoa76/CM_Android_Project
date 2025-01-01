package cm.project.cmproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cm.project.cmproject.models.OrderViewModel
import cm.project.cmproject.ui.AuthTabs
import cm.project.cmproject.ui.DeliveryDetailsScreen
import cm.project.cmproject.ui.HomeScreen
import cm.project.cmproject.ui.LobbyScreen
import cm.project.cmproject.ui.MapScreen
import cm.project.cmproject.ui.Navbar
import cm.project.cmproject.ui.OrderScreen
import cm.project.cmproject.ui.ProfileScreen
import cm.project.cmproject.ui.QrCodeScannerScreen
import cm.project.cmproject.ui.StepCreationScreen
import cm.project.cmproject.viewModels.AddressViewModel
import cm.project.cmproject.viewModels.DeliveryHistoryViewModel
import cm.project.cmproject.viewModels.DeliveryViewModel
import cm.project.cmproject.viewModels.MapViewModel
import cm.project.cmproject.viewModels.PendingDeliveriesViewModel
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
    val mapViewModel: MapViewModel = viewModel()
    val addressViewModel: AddressViewModel = viewModel()
    val deliveryHistoryViewModel: DeliveryHistoryViewModel = viewModel()
    val pendingDeliveriesViewModel: PendingDeliveriesViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("auth") {
            AuthTabs(navController = navController, viewModel = userViewModel)
        }
        composable("home") {
            Navbar(navController, userViewModel) {
                HomeScreen(
                    navController = navController,
                    userViewModel = userViewModel,
                    deliveryHistoryViewModel = deliveryHistoryViewModel
                )
            }
        }
        composable("profile") {
            Navbar(navController, userViewModel) {
                ProfileScreen(
                    userViewModel = userViewModel,
                    addressViewModel = addressViewModel,
                    navController = navController
                )
            }
        }
        composable("order") {
            val user by userViewModel.state.collectAsState()
            if (user == null) navController.navigate("auth")
            Navbar(navController, userViewModel) {
                deliveryHistoryViewModel.loadCurrentDeliveries(user!!.uid)
                OrderScreen(
                    mockViewModel = orderViewModel,
                    deliveryHistoryViewModel = deliveryHistoryViewModel,
                    navController = navController
                )
            }
        }
        composable("deliveryDetails/{deliveryId}") { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getString("deliveryId")
            if (deliveryId != null) {
                DeliveryDetailsScreen(
                    deliveryId = deliveryId,
                    navController = navController,
                    deliveryViewModel = deliveryViewModel,
                    userViewModel = userViewModel
                )
            }
        }
        composable("qrCodeScanner") {
            QrCodeScannerScreen(navController = navController, viewModel = deliveryViewModel)
        }

        composable("lobby") {
            val user by userViewModel.state.collectAsState()
            if (user != null && user!!.role == "driver") {
                Navbar(navController, userViewModel) {
                    LobbyScreen(
                        navController = navController,
                        userViewModel = userViewModel,
                        pendingDeliveriesViewModel = pendingDeliveriesViewModel
                    )
                }
            } else {
                AuthTabs(navController = navController, viewModel = userViewModel)
            }
        }

        composable("addDeliveryStep") {
            StepCreationScreen(
                deliveryViewModel = deliveryViewModel,
                navController = navController
            )
        }
        composable("mapScreen") {
            MapScreen(
                navController = navController,
                mapViewModel = mapViewModel,
                deliveryViewModel = TODO(),
                addressType = TODO()
            )
        }
        composable(
            route = "mapScreen?addressType={addressType}",
            arguments = listOf(navArgument("addressType") { defaultValue = "fromAddress" })
        ) { backStackEntry ->
            val addressType = backStackEntry.arguments?.getString("addressType") ?: "fromAddress"
            MapScreen(mapViewModel, deliveryViewModel, navController, addressType)
        }
        /*
        composable("createNewOrder") {
            Navbar(navController) {
                NewOrderScreen(
                    navController = navController,
                    deliveryViewModel = deliveryViewModel
                )
            }
        }*/
    }
}