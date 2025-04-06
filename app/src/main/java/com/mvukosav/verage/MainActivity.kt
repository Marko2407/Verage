package com.mvukosav.verage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mvukosav.verage.common.Screens.HOME
import com.mvukosav.verage.common.Screens.LOGIN
import com.mvukosav.verage.common.Screens.SCANNER
import com.mvukosav.verage.presentation.HomeScreen
import com.mvukosav.verage.presentation.LoginScreen
import com.mvukosav.verage.presentation.LoginViewModel
import com.mvukosav.verage.presentation.ScanScreen
import com.mvukosav.verage.ui.theme.VerageTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VerageTheme {
                val navController = rememberNavController()
                val loginViewModel: LoginViewModel = hiltViewModel()
                val isLoggedIn by loginViewModel.isLoggedIn.collectAsState(initial = null)

                when (isLoggedIn) {
                    null -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        val startDestination = if (isLoggedIn == true) HOME else LOGIN

                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable(LOGIN) {
                                LoginScreen(onLoginSuccess = {
                                    navController.navigate(HOME) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                })
                            }

                            composable(HOME) {
                                HomeScreen(
                                    onScanClick = { navController.navigate(SCANNER) },
                                    onLogoutClick = {
                                        loginViewModel.logout()
                                        navController.navigate(LOGIN) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(SCANNER) {
                                ScanScreen(onBack = {
                                    navController.popBackStack(HOME, inclusive = false)
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}