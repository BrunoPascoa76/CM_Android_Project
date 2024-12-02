package cm.project.cmproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.viewModels.DeliveryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.R
import cm.project.cmproject.viewModels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun DeliveryDetailsScreen(modifier: Modifier =Modifier, deliveryId: Int = 123, navController: NavController=rememberNavController(), deliveryViewModel: DeliveryViewModel= viewModel(), userViewModel: UserViewModel= viewModel()) {
    deliveryViewModel.fetchDelivery(deliveryId)
    val delivery by deliveryViewModel.state.collectAsStateWithLifecycle()

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
                ErrorMessage(deliveryViewModel)
                OrderDetails(deliveryViewModel = deliveryViewModel, userViewModel = userViewModel, navController = navController)
            }
        }
    }
}

@Composable
fun OrderDetails(
    modifier: Modifier = Modifier,
    deliveryViewModel: DeliveryViewModel,
    userViewModel: UserViewModel,
    navController: NavController
){
    val delivery by deliveryViewModel.state.collectAsStateWithLifecycle()
    val recipient by deliveryViewModel.recipient.collectAsStateWithLifecycle()
    val sender by deliveryViewModel.sender.collectAsStateWithLifecycle()
    val user by userViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier=modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ){
        ElevatedCard{
            Column(modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DetailsRow("Title", delivery?.parcel?.description?:"N/A")
                DetailsRow("Status", delivery?.status?:"Unknown")
                if(recipient!=null)
                    DetailsRow("Recipient Name", recipient!!.fullName)
                if(sender!=null) {
                    DetailsRow("Sender Name", sender!!.fullName)
                    DetailsRow("Sender Address", sender!!.address)
                }
            }
        }
        //TODO: add steps
        if(user!=null && user!!.role=="customer"){
            QrCode(deliveryId = delivery!!.deliveryId)
        }else{
            ElevatedButton(
                onClick = {}
            ){
                Row{
                    Icon(imageVector= ImageVector.vectorResource(id = R.drawable.qr_code_scanner_24px), contentDescription = "Scan QR Code")
                    Text("Enter qr code")
                }
            }
        }
    }
}

@Composable
private fun QrCode(deliveryId: Int) {
    //TODO: generate qr code
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
