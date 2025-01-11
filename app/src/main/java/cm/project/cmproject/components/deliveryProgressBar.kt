package cm.project.cmproject.components

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.cmproject.models.Delivery
import cm.project.cmproject.viewModels.DeliveryHistoryViewModel
import cm.project.cmproject.viewModels.DeliveryViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun DeliveryProgressBar(
    modifier: Modifier = Modifier,
    deliveryViewModel: DeliveryViewModel = viewModel()
) {

    val delivery by deliveryViewModel.state.collectAsState()
    val driverLocation by deliveryViewModel.currentLocation.collectAsState()

    LaunchedEffect(delivery?.deliveryId) {
        if (delivery?.deliveryId != null) {
            deliveryViewModel.listenForDeliveryStatusUpdates()
        }
    }

    DeliveryProgressBarComponent(delivery, driverLocation, modifier)

    DisposableEffect(Unit) {
        onDispose {
            deliveryViewModel.detachListener()
        }
    }
}

@Composable
private fun DeliveryProgressBarComponent(
    delivery: Delivery?,
    driverLocation: LatLng?,
    modifier: Modifier = Modifier
) {

    if (delivery != null) {
        val completedSteps = delivery.completedSteps
        val steps = delivery.steps
        val progress = calculateProgress(driverLocation, delivery)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(state = rememberScrollState()),
            horizontalArrangement = Arrangement.Center
        ) {
            if (steps.isEmpty()) {
                Text("No steps available", style = TextStyle(fontSize = 16.sp))
            } else {
                for (i in steps.indices) {
                    val step = steps[i]

                    StepCircle(
                        step = step.description,
                        isCompleted = i < completedSteps
                    )
                    if (i < steps.size - 1) {
                        val barProgress =
                            (if (i < completedSteps - 1) 1f else (if (i == completedSteps - 1) progress else 0f)).toFloat()
                        ProgressBar(progress = barProgress)
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryProgressBar(
    modifier: Modifier = Modifier,
    deliveryHistoryViewModel: DeliveryHistoryViewModel = viewModel(),
    index: Int = 0
) {
    val deliveries by deliveryHistoryViewModel.currentDeliveries.collectAsState()
    val driverLocation by deliveryHistoryViewModel.currentLocation.collectAsState()

    val delivery = deliveries.getOrNull(index)

    DeliveryProgressBarComponent(delivery, driverLocation, modifier)
}

@Composable
fun StepCircle(
    step: String,
    isCompleted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        )
        Text(
            text = step,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.padding(top = 15.dp, start = 4.dp, end = 2.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

fun calculateProgress(driverLocation: LatLng?, delivery: Delivery): Int {
    // lack of data
    if (driverLocation == null) return 0
    if (delivery.steps.isEmpty()) return 0

    //at the edges
    if (delivery.completedSteps == 0) return 0
    if (delivery.completedSteps == delivery.steps.size) return 100

    val lastStep = delivery.steps.getOrNull(delivery.completedSteps - 1) ?: return 0
    val currentStep = delivery.steps.getOrNull(delivery.completedSteps) ?: return 0


    //one of the steps has unset location (it's not null, but it should be treated as if it is)
    if (lastStep.location.address == "" || currentStep.location.address == "") return 0

    val previousLocation = Location("previousLocation")
    previousLocation.latitude = lastStep.location.latitude
    previousLocation.longitude = lastStep.location.longitude

    val nextLocation = Location("nextLocation")
    nextLocation.latitude = currentStep.location.latitude
    nextLocation.longitude = currentStep.location.longitude

    val currentLocation = Location("currentLocation")
    currentLocation.latitude = driverLocation.latitude
    currentLocation.longitude = driverLocation.longitude

    val totalDistance = previousLocation.distanceTo(nextLocation)
    val currentDistance = currentLocation.distanceTo(nextLocation)

    if (totalDistance == 0f) return 0 //avoid division by zero

    return (((totalDistance - currentDistance) / totalDistance) * 100).toInt()
}