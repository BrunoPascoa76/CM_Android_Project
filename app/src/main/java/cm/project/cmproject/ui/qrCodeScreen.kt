package cm.project.cmproject.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }


    when(cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            Column(modifier = modifier.fillMaxSize()) {
                AndroidView(factory = { context ->
                    val previewView = PreviewView(context)
                    startCamera(previewView, viewModel, navController)
                    previewView
                },modifier=Modifier.fillMaxSize())
            }
        }
        is PermissionStatus.Denied -> {
            Text("Camera permission denied. Cannot scan QR code.")
        }
    }
}

private fun startCamera(previewView: PreviewView, viewModel: DeliveryViewModel, navController: NavController) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(previewView.context), BarcodeAnalyzer { result ->
                    viewModel.submitQRCode(result)
                    navController.navigateUp()
                })
            }

        cameraProvider.bindToLifecycle(
            previewView.context as LifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
    }, ContextCompat.getMainExecutor(previewView.context))
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

