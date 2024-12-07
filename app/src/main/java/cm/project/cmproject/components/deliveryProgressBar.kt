package cm.project.cmproject.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cm.project.cmproject.viewModels.DeliveryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.rememberTextMeasurer
import com.google.maps.android.compose.Circle

@Composable
fun DeliveryProgressBar(viewModel: DeliveryViewModel = viewModel()) {
    val textMeasurer = rememberTextMeasurer()
    val delivery by viewModel.state.collectAsState()

    if (delivery == null) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    } else {
        val completedSteps = delivery!!.completedSteps
        val steps = delivery!!.steps
        val progress = 0.5 //TODO: change to real value (once we get realtime db up and running)
        Row(modifier=Modifier.fillMaxWidth().horizontalScroll(state=rememberScrollState()), horizontalArrangement = Arrangement.Center) {
            if (steps.isEmpty()) {
                Text("No steps available", style = TextStyle(fontSize = 16.sp))
            }else{
                for (i in steps.indices) {
                    val step = steps[i]

                    StepCircle(
                        step = step.description,
                        isCompleted = i < completedSteps,
                        isCurrentStep = i == completedSteps
                    )
                    if (i < steps.size - 1) {
                        val barProgress= (if (i<completedSteps) 1f else (if(i==completedSteps) progress else 0f)).toFloat()
                        ProgressBar(progress= barProgress)
                    }
                }
            }
        }
    }
}

@Composable
fun StepCircle(
    step: String,
    isCompleted: Boolean,
    isCurrentStep: Boolean
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
    Box(modifier=Modifier.padding(top=15.dp,start=4.dp,end=2.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}