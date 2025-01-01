package cm.project.cmproject.components

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val deliveryId = inputData.getString("deliveryId") ?: return Result.failure()
        val location = fetchUserLocation(applicationContext) ?: return Result.failure()

        sendDriverLocation(deliveryId, location)
        return Result.success()
    }
}

fun sendDriverLocation(deliveryId: String, location: LatLng) {
    val database =
        FirebaseDatabase.getInstance("https://cm-android-2024-default-rtdb.europe-west1.firebasedatabase.app/")
    val deliveryStatusRef = database.getReference("deliveryStatus").child(deliveryId)

    val deliveryStatus = mapOf(
        "status" to "IN_TRANSIT",
        "timestamp" to System.currentTimeMillis(),
        "latitude" to location.latitude,
        "longitude" to location.longitude
    )

    deliveryStatusRef.setValue(deliveryStatus).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Timber.d("Driver location updated successfully.")
        } else {
            Timber.e("Failed to update driver location: ${task.exception?.message}")
        }
    }
}

suspend fun fetchUserLocation(context: Context): LatLng? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        suspendCancellableCoroutine { continuation ->
            LocationManagerCompat.getCurrentLocation(
                locationManager,
                LocationManager.GPS_PROVIDER,
                null as android.os.CancellationSignal?,
                ContextCompat.getMainExecutor(context)
            ) { location: Location? ->
                if (location != null) {
                    continuation.resume(LatLng(location.latitude, location.longitude))
                } else {
                    continuation.resumeWithException(Exception("Failed to get location"))
                }
            }
        }
    } else {
        Timber.e("Location permission is not granted.")
        null
    }
}