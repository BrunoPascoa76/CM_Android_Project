package cm.project.cmproject.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun DeliveryDetailsScreen(modifier: Modifier =Modifier, deliveryId: String? = null, navController: NavController=rememberNavController()) {
    val delivery:Delivery?

    Scaffold(
        modifier=modifier,
        topBar= { CenterAlignedTopAppBar(title={Text("Delivery Details")}) }
    ) { innerPadding->
        Column(modifier=Modifier.padding(innerPadding)) {
            Text("abc")
        }
    }
}