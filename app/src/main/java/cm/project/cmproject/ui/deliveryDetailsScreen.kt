package cm.project.cmproject.ui

import android.transition.CircularPropagation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.viewModels.DeliveryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.viewModels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun DeliveryDetailsScreen(modifier: Modifier =Modifier, deliveryId: Int = 123, navController: NavController=rememberNavController(), viewModel: DeliveryViewModel= viewModel()) {
    viewModel.fetchDelivery(deliveryId)
    val delivery by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier=modifier,
        topBar= { CenterAlignedTopAppBar(title={Text("Delivery Details")}) }
    ) { innerPadding->
        if (delivery==null){
            Column(
                modifier=Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }else {
            Column(modifier = Modifier.padding(innerPadding)) {
                ErrorMessage(viewModel)
                OrderDetails(delivery = delivery!!)
            }
        }
    }
}

@Composable
fun OrderDetails(modifier: Modifier = Modifier, delivery: Delivery){
    ElevatedCard(modifier=modifier.padding(10.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailsRow("Title", delivery.parcel.description)
            DetailsRow("Status", delivery.status)
        }
    }
}

@Composable
private fun DetailsRow(title:String, value:String) {
    Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${title}:",fontWeight= FontWeight.Bold)
        Text(value)
    }
}

@Composable
fun ErrorMessage(viewModel: DeliveryViewModel) {
    val errorMessage: String? by viewModel.errorMessage.collectAsState()
    if (errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = errorMessage!!,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
