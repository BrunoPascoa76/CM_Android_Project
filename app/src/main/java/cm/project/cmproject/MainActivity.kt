package cm.project.cmproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import cm.project.cmproject.components.LocationWorker
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.repositories.DeliveryRepository
import cm.project.cmproject.ui.navigation.AppNavHost
import cm.project.cmproject.ui.theme.CMProjectTheme
import cm.project.cmproject.viewModels.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

                if (user != null) {
                    viewModel.fetchUserTable(user.uid)
                    if (viewModel.state.value?.role == "driver") {
                        val deliveryRepository = DeliveryRepository()
                        lifecycleScope.launch {
                            val deliveries =
                                deliveryRepository.getDeliveryById(user.uid) as List<Delivery>
                            deliveries.forEach { delivery ->
                                scheduleLocationUpdates(delivery.deliveryId, this@MainActivity)
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
    val locationWorkRequest = PeriodicWorkRequestBuilder<LocationWorker>(1, TimeUnit.MINUTES)
        .setInputData(workDataOf("deliveryId" to deliveryId))
        .build()

    workManager.enqueueUniquePeriodicWork(
        "LocationUpdateWork",
        ExistingPeriodicWorkPolicy.UPDATE,
        locationWorkRequest
    )
}
