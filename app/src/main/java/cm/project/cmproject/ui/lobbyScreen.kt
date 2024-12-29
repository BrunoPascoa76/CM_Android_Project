package cm.project.cmproject.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cm.project.cmproject.viewModels.PendingDeliveriesViewModel
import cm.project.cmproject.viewModels.UserViewModel
import timber.log.Timber

@Composable
fun LobbyScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    pendingDeliveriesViewModel: PendingDeliveriesViewModel = viewModel()
) {
    val user by userViewModel.state.collectAsState()

    if (user == null || user!!.role != "driver") {
        navController.navigate("home") //you shouldn't be here
    }

    //handle subscribe/unsubscribe
    DisposableEffect(Unit) {
        pendingDeliveriesViewModel.subscribeToPendingDeliveries()
        Timber.tag("pendingDeliveries").d("Subscribed")

        onDispose {
            pendingDeliveriesViewModel.unsubscribeFromPendingDeliveries()
            Timber.tag("pendingDeliveries").d("Unsubscribed")
        }
    }
}