package cm.project.cmproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.ui.AuthTabs
import cm.project.cmproject.ui.HomeScreen
import cm.project.cmproject.ui.Navbar
import cm.project.cmproject.ui.ProfileScreen
import cm.project.cmproject.ui.theme.CMProjectTheme
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val user = auth.currentUser
                    AppNavHost(modifier = Modifier.padding(innerPadding), startDestination = if (user == null) "auth" else "home")
                }
            }
        }
    }
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController(),startDestination: String = "auth") {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable("auth") {
            AuthTabs()
        }
        composable("home") {
            Navbar(navController){
                HomeScreen()
            }
        }
        composable("Profile"){
            Navbar(navController){
                ProfileScreen()
            }
        }
    }
}