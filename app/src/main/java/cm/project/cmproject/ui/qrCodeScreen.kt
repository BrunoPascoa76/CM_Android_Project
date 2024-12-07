package cm.project.cmproject.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cm.project.cmproject.viewModels.DeliveryViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrCodeScannerScreen(modifier:Modifier=Modifier,viewModel: DeliveryViewModel, navController: NavController=rememberNavController()) {
    val cameraPermissionState=rememberPermissionState(android.Manifest.permission.CAMERA)

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }


    when(cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            Box(modifier = modifier.fillMaxSize()) {
                AndroidView(factory = { context ->
                    val previewView = PreviewView(context)
                    cameraProvider=startCamera(previewView, viewModel, navController)
                    previewView
                },modifier=Modifier.fillMaxSize())
                Row(modifier=Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Start){
                    IconButton(
                        modifier=Modifier.size(80.dp),
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(imageVector= Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",tint= Color.White)
                    }
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    cameraProvider?.unbindAll() // Unbind all use cases
                }
            }
        }
        is PermissionStatus.Denied -> {
            Text("Camera permission denied. Cannot scan QR code.")
        }
    }
}

private fun startCamera(previewView: PreviewView, viewModel: DeliveryViewModel, navController: NavController): ProcessCameraProvider {
    val cameraProvider: ProcessCameraProvider=ProcessCameraProvider.getInstance(previewView.context).get()

    val preview = Preview.Builder().build().also {
        it.surfaceProvider = previewView.surfaceProvider
    }

    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build().also {
            it.setAnalyzer(ContextCompat.getMainExecutor(previewView.context), BarcodeAnalyzer { result ->
                viewModel.submitQRCode(result)
                navController.navigateUp()
            })
        }

    cameraProvider.bindToLifecycle( // Use cameraProvider?. to handle null
        previewView.context as LifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        imageAnalysis
    )

    return cameraProvider
}


class BarcodeAnalyzer(private val onBarcodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        BarcodeScanning.getClient().process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    onBarcodeDetected(barcode.displayValue ?: "")
                }
            }
            .addOnCompleteListener { imageProxy.close() } // Close the image proxy
    }
}

