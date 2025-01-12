package cm.project.cmproject.components

import android.location.Location
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
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
    val scrollState = rememberScrollState()
    LaunchedEffect(delivery?.deliveryId) {
        scrollState.animateScrollTo(
            scrollState.maxValue,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    if (delivery != null) {
        val completedSteps = delivery.completedSteps
        val steps = delivery.steps
        val progress = calculateProgress(driverLocation, delivery)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(state = scrollState),
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
                        if (i == completedSteps - 1) {
                            ProgressBar(progress = 1f, animate = true)
                        } else {
                            ProgressBar(progress = barProgress)
                        }
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
    driverLocation: LatLng? = null,
    index: Int = 0
) {
    val deliveries by deliveryHistoryViewModel.currentDeliveries.collectAsState()

    //need to do this to not have to replicate everything
    val deliveryViewModel: DeliveryViewModel = viewModel()
    deliveryViewModel.fetchDelivery(deliveries[index].deliveryId)

    LaunchedEffect(deliveries[index].deliveryId) {
        deliveryViewModel.listenForDeliveryStatusUpdates()
    }

    DisposableEffect(Unit) {
        onDispose {
            deliveryViewModel.detachListener()
        }
    }

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
    animate: Boolean = false,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(modifier = Modifier.padding(top = 15.dp, start = 4.dp, end = 2.dp)) {
        if (animate) {
            AnimatedLoadingIndicator(modifier = modifier, color = color)
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = modifier
                    .height(10.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
            )
        }
    }
}

@Composable
fun AnimatedLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .height(10.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = color
    )
}


fun calculateProgress(driverLocation: LatLng?, delivery: Delivery): Float {
    // lack of data
    if (driverLocation == null) return 0f
    if (delivery.steps.isEmpty()) return 0f

    //at the edges
    if (delivery.completedSteps == 0) return 0f
    if (delivery.completedSteps == delivery.steps.size) return 100f

    val lastStep = delivery.steps.getOrNull(delivery.completedSteps - 1) ?: return 0f
    val currentStep = delivery.steps.getOrNull(delivery.completedSteps) ?: return 0f

    //one of the steps has unset location (it's not null, but it should be treated as if it is)
    if (lastStep.location.address == "" || currentStep.location.address == "") return 0f

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

    if (totalDistance == 0f) return 0f //avoid division by zero

    return (totalDistance - currentDistance) / totalDistance
}