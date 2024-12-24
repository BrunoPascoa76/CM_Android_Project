package cm.project.cmproject.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.R
import cm.project.cmproject.viewModels.DeliveryHistoryViewModel
import cm.project.cmproject.viewModels.UserViewModel

@Composable
@Preview(showBackground = true)
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    userViewModel: UserViewModel = viewModel(),
    deliveryHistoryViewModel: DeliveryHistoryViewModel = viewModel()
) {
    val user by userViewModel.state.collectAsState()

    user?.uid?.let {
        deliveryHistoryViewModel.loadCurrentDeliveries(it)
        deliveryHistoryViewModel.loadPastDeliveries(it)
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.size(100.dp))
        Text(text = "Hello, ${user?.fullName}", fontSize = 30.sp)
        CurrentDeliveriesSection(
            viewModel = deliveryHistoryViewModel,
            navController = navController
        )
        PastDeliveriesSection(viewModel = deliveryHistoryViewModel, navController = navController)
    }
}

@Composable
fun CurrentDeliveriesSection(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: DeliveryHistoryViewModel = viewModel()
) {
    val currentDeliveries by viewModel.currentDeliveries.collectAsState()

    ElevatedCard(modifier = modifier
        .fillMaxWidth()
        .padding(10.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "Current Deliveries:", fontSize = 20.sp)
            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                item {
                    Button(
                        onClick = {}, //TODO: navigate to create order screen
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "create order")
                        Text(text = "Create Order")
                    }
                }
                if (currentDeliveries.isEmpty()) {
                    item {
                        Text(text = "No current deliveries", modifier = Modifier.padding(10.dp))
                    }
                } else {
                    items(currentDeliveries.size) {
                        val delivery = currentDeliveries[it]
                        Button(
                            onClick = { navController.navigate("deliveryDetails/${delivery.deliveryId}") },
                            modifier = Modifier.padding(10.dp)
                        ) {
                            when (delivery.status) {
                                "Pending" -> Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "pending"
                                )

                                "Accepted" -> Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "accepted"
                                )

                                "In Transit" -> Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.local_shipping_24px),
                                    contentDescription = "in transit"
                                )

                                else -> Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "unknown"
                                )
                            }
                            Text(text = "Delivery ${delivery.parcel.description}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PastDeliveriesSection(
    modifier: Modifier = Modifier,
    viewModel: DeliveryHistoryViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val pastDeliveries by viewModel.pastDeliveries.collectAsState()

    ElevatedCard(modifier = modifier
        .fillMaxWidth()
        .padding(10.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "Past Deliveries:", fontSize = 20.sp)
            if (pastDeliveries.isNotEmpty()) {
                LazyColumn {
                    items(pastDeliveries.size) {
                        val delivery = pastDeliveries[it]
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    navController.navigate("deliveryDetails/${delivery.deliveryId}")
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    delivery.parcel.description,
                                    modifier = Modifier.padding(10.dp)
                                )
                                if (delivery.steps.isNotEmpty()) {
                                    Text(
                                        delivery.steps[delivery.steps.size - 1].completionDate.toString(),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(text = "No past deliveries", modifier = Modifier.padding(10.dp))
            }
        }
    }
}

