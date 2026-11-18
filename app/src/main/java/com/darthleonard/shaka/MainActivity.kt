package com.darthleonard.shaka

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darthleonard.shaka.core.permissions.PermissionManager
import com.darthleonard.shaka.presentation.sonar.SonarScreen
import com.darthleonard.shaka.presentation.sonar.SonarViewModel
import com.darthleonard.shaka.presentation.splash.SplashScreen
import com.darthleonard.shaka.presentation.theme.ShakaTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val permState = PermissionManager.checkPermissions(this)
        sonarViewModel.updatePermissionStatus(permState.isWifiScanningAllowed)
    }

    private val sonarViewModel: SonarViewModel by viewModels {
        val app = application as ShakaApplication
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SonarViewModel(
                    sessionManager = app.sessionManager,
                    wifiRepository = app.wifiRepository,
                    settingsRepository = app.settingsRepository,
                    eventLogRepository = app.eventLogRepository,
                    capabilityDetector = app.capabilityDetector
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permState = PermissionManager.checkPermissions(this)
        sonarViewModel.updatePermissionStatus(permState.isWifiScanningAllowed)

        setContent {
            ShakaTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                            if (!sonarViewModel.hasPermissions.value) {
                                requestAppPermissions()
                            }
                        }
                    )
                } else {
                    val isSessionActive by sonarViewModel.isSessionActive.collectAsState()

                    // Keep screen awake while sonar session is running
                    LaunchedEffect(isSessionActive) {
                        if (isSessionActive) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }

                    SonarScreen(
                        viewModel = sonarViewModel,
                        onRequestPermissions = { requestAppPermissions() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val permState = PermissionManager.checkPermissions(this)
        sonarViewModel.updatePermissionStatus(permState.isWifiScanningAllowed)
    }

    private fun requestAppPermissions() {
        val permissions = PermissionManager.getRequiredPermissions().toTypedArray()
        permissionLauncher.launch(permissions)
    }
}

