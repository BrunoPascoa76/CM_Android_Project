package cm.project.cmproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cm.project.cmproject.components.LocationWorker
import cm.project.cmproject.repositories.DeliveryRepository
import cm.project.cmproject.repositories.Result
import cm.project.cmproject.ui.navigation.AppNavHost
import cm.project.cmproject.ui.theme.CMProjectTheme
import cm.project.cmproject.viewModels.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.time.Duration

class MainActivity : ComponentActivity() {
    @SuppressLint("StateFlowValueCalledInComposition", "CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val auth: FirebaseAuth = Firebase.auth

        enableEdgeToEdge()
        setContent {
            CMProjectTheme {
                val user = auth.currentUser
                val viewModel: UserViewModel = viewModel()

                // Use LaunchedEffect to handle async operations
                LaunchedEffect(user) {
                    if (user != null) {
                        viewModel.fetchUserTable(user.uid)
                        // Collect state changes
                        viewModel.state.collect { state ->
                            if (state?.role == "driver") {
                                when (val result = DeliveryRepository().getAllByUserIdAndStatus(
                                    user.uid,
                                    listOf(
                                        "Accepted",
                                        "Pickup",
                                        "In Transit"
                                    )
                                )) {
                                    is Result.Success -> {
                                        val deliveries = result.data
                                        deliveries.forEach { delivery ->
                                            scheduleLocationUpdates(
                                                delivery.deliveryId,
                                                this@MainActivity
                                            )
                                        }
                                    }

                                    is Result.Error -> {}
                                }
                            }
                        }
                    }
                }

                AppNavHost(
                    startDestination = if (user == null) "auth" else "home",
                )

            }
        }
    }
}

fun scheduleLocationUpdates(deliveryId: String, context: Context) {
    val workManager = WorkManager.getInstance(context)
    val workRequest = OneTimeWorkRequestBuilder<LocationWorker>()
        .setInitialDelay(Duration.ofSeconds(5))
        .setInputData(workDataOf("deliveryId" to deliveryId))
        .build()

    workManager.enqueueUniqueWork(
        deliveryId,
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}
