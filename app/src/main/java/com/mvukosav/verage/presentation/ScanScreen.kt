package com.mvukosav.verage.presentation

import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.microblink.ux.camera.compose.CameraScreen
import com.mvukosav.verage.CustomScannerViewModel

@Composable
fun ScanScreen(onBack: () -> Unit) {
    val viewModel: CustomScannerViewModel = hiltViewModel()
    val isLoading = viewModel.isLoading.value
    val isUserAdult = viewModel.isUserAdult.value
    val showPopup = viewModel.showResultPopup.value
    val showErrorPopup = viewModel.showErrorPopup.value
    val message by viewModel.overlayMessage.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            CameraScreen(cameraViewModel = viewModel) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {

                    val pulse = rememberInfiniteTransition(label = "pulse")
                    val scale by pulse.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = EaseInOutCirc),
                            repeatMode = RepeatMode.Reverse
                        ), label = "scale"
                    )
                    val alpha by pulse.animateFloat(
                        initialValue = 0.7f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.Center)
                            .scale(scale)
                            .alpha(alpha)
                            .graphicsLayer {
                                shape = CircleShape
                            }
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )

                    if (message.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 80.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                        8.dp
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = message,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Button(onClick = { viewModel.toggleTorch() }) {
                            Text(if (viewModel.isTorchOn.value) "Torch OFF" else "Torch ON")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Button(onClick = onBack) {
                            Text("✕")
                        }
                    }
                }
            }
        }

        if (showErrorPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.resetAndRestartSdk() },
                confirmButton = {
                    Button(onClick = { viewModel.resetAndRestartSdk() }) {
                        Text("Try again")
                    }
                },
                title = { Text("Scan Error") },
                text = { Text("Scanning timed out. Please try again.") }
            )
        }

        if (showPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.resetAndRestartSdk() },
                confirmButton = {
                    Button(onClick = { viewModel.resetAndRestartSdk() }) {
                        Text("OK")
                    }
                },
                title = { Text("Scan Result") },
                text = {
                    when (isUserAdult) {
                        true -> Text("User is an adult ✅")
                        false -> Text("User is under 18 ❌")
                        else -> Text("Could not determine age")
                    }
                }
            )
        }
    }
}
