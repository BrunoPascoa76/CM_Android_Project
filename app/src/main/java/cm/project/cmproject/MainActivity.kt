package cm.project.cmproject

import OrderViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import cm.project.cmproject.ui.theme.CMProjectTheme
import cm.project.cmproject.viewModels.DeliveryViewModel
import cm.project.cmproject.viewModels.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val auth: FirebaseAuth = Firebase.auth

        enableEdgeToEdge()
        setContent {
            CMProjectTheme {
                val user = auth.currentUser

                val viewModel: UserViewModel = viewModel()

                if (user != null) {
                    viewModel.fetchUserTable(user.uid)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        modifier = Modifier.padding(innerPadding),
                        startDestination = if (user == null) "auth" else "home"
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "auth"
) {
    val orderViewModel: OrderViewModel = viewModel() //TODO: change screens that use this to use DeliveryViewModel (will keep this for now until we no longer need mocks)
    val userViewModel: UserViewModel = viewModel()
    val deliveryViewModel: DeliveryViewModel = viewModel()
    //declare any viewModels here


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
        composable("deliveryDetails/{deliveryId}"){ backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getInt("deliveryId") ?: 0
            DeliveryDetailsScreen(
                deliveryId = deliveryId,
                navController = navController,
                deliveryViewModel = deliveryViewModel,
                userViewModel = userViewModel
            )
        }
        composable("order") {
            Navbar(navController) {
                OrderScreen(viewModel=orderViewModel,navController=navController)
            }
        }
    }
}