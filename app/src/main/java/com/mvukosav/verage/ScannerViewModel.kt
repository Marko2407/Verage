package com.mvukosav.verage

import android.content.Context
import android.os.Build
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.VibrationEffect.createOneShot
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microblink.blinkid.core.BlinkIdSdk
import com.microblink.blinkid.core.BlinkIdSdkSettings
import com.microblink.blinkid.core.session.BlinkIdScanningResult
import com.microblink.blinkid.core.session.BlinkIdSessionSettings
import com.microblink.blinkid.core.session.ScanningMode
import com.microblink.blinkid.core.settings.ScanningSettings
import com.microblink.blinkid.ux.scanning.BlinkIdAnalyzer
import com.microblink.blinkid.ux.scanning.BlinkIdDocumentLocatedLocation
import com.microblink.blinkid.ux.scanning.BlinkIdScanningDoneHandler
import com.microblink.blinkid.ux.settings.BlinkIdUxSettings
import com.microblink.ux.ScanningUxEvent
import com.microblink.ux.ScanningUxEventHandler
import com.microblink.ux.camera.CameraViewModel
import com.microblink.ux.utils.ErrorReason
import com.mvukosav.verage.common.Constants.LICENSE_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

@HiltViewModel
class CustomScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraViewModel() {

    val isLoading = mutableStateOf(true)
    val isUserAdult = mutableStateOf<Boolean?>(null)
    val showResultPopup = mutableStateOf(false)
    val showErrorPopup = mutableStateOf(false)
    val overlayMessage = MutableStateFlow("")
    val isTorchOn = mutableStateOf(false)

    private var lastMessage: String = ""
    private var lastMessageTimestamp = 0L
    private val messageThrottleMillis = 2000L

    private var analyzer: BlinkIdAnalyzer? = null
    private var hasVibratedForLocated = false
    private var hasVibratedForSuccess = false

    init {
        initializeBlinkSdk()
    }

    fun toggleTorch() {
        isTorchOn.value = !isTorchOn.value
        _torchOn.value = isTorchOn.value
    }

    private fun vibrateOnce() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(
            createOneShot(
                150,
                DEFAULT_AMPLITUDE
            )
        )
    }

    private fun initializeBlinkSdk() {
        isLoading.value = true
        viewModelScope.launch {
            val sdkResult = BlinkIdSdk.initializeSdk(
                context,
                BlinkIdSdkSettings(
                    licenseKey = LICENSE_KEY,
                    downloadResources = true
                )
            )

            val sdkInstance = sdkResult.getOrNull()
            if (sdkInstance != null) {
                analyzer = BlinkIdAnalyzer(
                    blinkIdSdk = sdkInstance,
                    sessionSettings = BlinkIdSessionSettings(
                        scanningMode = ScanningMode.Single,
                        scanningSettings = ScanningSettings()
                    ),
                    uxSettings = BlinkIdUxSettings(),
                    scanningDoneHandler = object : BlinkIdScanningDoneHandler {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onScanningFinished(result: BlinkIdScanningResult) {
                            if (!hasVibratedForSuccess) {
                                vibrateOnce()
                                hasVibratedForSuccess = true
                            }

                            val dob = result.dateOfBirth
                            val y = dob?.year
                            val m = dob?.month
                            val d = dob?.day
                            isUserAdult.value = try {
                                val birthDate = LocalDate.of(y!!, m!!, d!!)
                                Period.between(birthDate, LocalDate.now()).years >= 18
                            } catch (e: Exception) {
                                null
                            }
                            showResultPopup.value = true
                        }

                        override fun onScanningCanceled() {}

                        override fun onError(error: ErrorReason) {
                            if (error == ErrorReason.ErrorTimeoutExpired) {
                                vibrateOnce()
                                showErrorPopup.value = true
                            } else {
                                resetAndRestartSdk()
                            }
                        }
                    },
                    uxEventHandler = object : ScanningUxEventHandler {
                        override fun onUxEvents(events: List<ScanningUxEvent>) {
                            val now = System.currentTimeMillis()
                            for (event in events) {

                                val message = when (event) {
                                    is ScanningUxEvent.DocumentLocated, is BlinkIdDocumentLocatedLocation -> {
                                        if (!hasVibratedForLocated) {
                                            vibrateOnce()
                                            hasVibratedForLocated = true
                                        }
                                        "Document located ✅"
                                    }

                                    is ScanningUxEvent.DocumentTooFar -> "Move document closer 📷"
                                    is ScanningUxEvent.DocumentTooClose -> "Move document away ⬅️"

                                    is ScanningUxEvent.DocumentNotFound -> {
                                        hasVibratedForLocated = false
                                        "Adjust document position 🧾✂️"
                                    }

                                    is ScanningUxEvent.DocumentNotFullyVisible -> {
                                        "Adjust document position 🧾✂️"
                                    }

                                    else -> null
                                }

                                if (message != null &&
                                    (message != lastMessage && now - lastMessageTimestamp > messageThrottleMillis)
                                ) {
                                    overlayMessage.value = message
                                    lastMessage = message
                                    lastMessageTimestamp = now
                                }
                            }
                        }
                    }
                )
            } else {
                Log.e("LOLOLO", "SDK failed to initialize")
            }

            isLoading.value = false
        }
    }

    override fun analyzeImage(image: ImageProxy) {
        image.use {
            analyzer?.analyze(it)
        }
    }

    fun resetAndRestartSdk() {
        isUserAdult.value = null
        showResultPopup.value = false
        showErrorPopup.value = false
        hasVibratedForLocated = false
        hasVibratedForSuccess = false
        analyzer?.cancel()
        analyzer?.close()
        initializeBlinkSdk()
    }

    override fun onCleared() {
        analyzer?.cancel()
        analyzer?.close()
    }
}

class CustomScannerViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomScannerViewModel(context) as T
    }
}