package cm.project.cmproject.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.viewModels.PendingDeliveriesViewModel
import cm.project.cmproject.viewModels.UserViewModel
import timber.log.Timber

@Composable
@Preview(showBackground = true)
fun LobbyScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    userViewModel: UserViewModel = viewModel(),
    pendingDeliveriesViewModel: PendingDeliveriesViewModel = viewModel()
) {
    val user by userViewModel.state.collectAsState()
    val pendingDeliveries by pendingDeliveriesViewModel.state.collectAsState()
    val acceptedDelivery by pendingDeliveriesViewModel.acceptedDelivery.collectAsState()

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

    LaunchedEffect(acceptedDelivery) {
        if (acceptedDelivery) {
            navController.navigate("track")
        }
    }

    if (pendingDeliveries.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 10.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.orders_24px),
                contentDescription = "No deliveries",
            )
            Text(
                text = "No pending deliveries...",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
        }
    } else {
        LazyColumn {
            items(pendingDeliveries.size) { index ->
                val pendingDelivery = pendingDeliveries[index]
                PendingDeliveryCard(
                    pendingDeliveriesViewModel = pendingDeliveriesViewModel,
                    userViewModel = userViewModel,
                    pendingDelivery = pendingDelivery
                )
            }
        }
    }
}

@Composable
fun PendingDeliveryCard(
    modifier: Modifier = Modifier,
    pendingDeliveriesViewModel: PendingDeliveriesViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    pendingDelivery: Delivery
) {
    var isExpanded by remember { mutableStateOf(false) }
    val user by userViewModel.state.collectAsState()

    Card(modifier = modifier
        .padding(5.dp)
        .clickable {
            isExpanded = !isExpanded
        }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Delivery ID:", fontWeight = FontWeight.Bold)
                Text(pendingDelivery.deliveryId, fontWeight = FontWeight.Bold)
            }

            if (isExpanded) {
                //they are declared here for efficiency's sake (in hope that only expanded cards will be rebuilt
                val recipient by pendingDeliveriesViewModel.selectedRecipient.collectAsState()
                val sender by pendingDeliveriesViewModel.selectedSender.collectAsState()
                pendingDeliveriesViewModel.fetchRecipientAndSender(pendingDelivery)



                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recipient Address:", modifier = Modifier.weight(0.3f))
                    Text(
                        recipient?.address?.address ?: "Loading...",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.7f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sender Address:", modifier = Modifier.weight(0.3f))
                    Text(
                        sender?.address?.address ?: "Loading...",
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Button(onClick = {
                    pendingDeliveriesViewModel.assignSelfToDelivery(pendingDelivery, user!!.uid)
                }) {
                    Row {
                        Icon(Icons.Default.Check, contentDescription = "Check")
                        Text("Accept")
                    }
                }
            }
        }
    }
}