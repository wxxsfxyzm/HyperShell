package app.hypershell.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.hypershell.ui.page.main.navigation.NavHost
import app.hypershell.ui.theme.HyperShellTheme
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.Surface as Material3Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            val mainViewModel: MainViewModel = koinViewModel()
            val uiState by mainViewModel.uiState.collectAsState()
            splashScreen.setKeepOnScreenCondition { !uiState.isLoaded }
            if (uiState.isLoaded) HyperShellTheme(
                useMiuix = uiState.useMiuix,
                themeMode = uiState.themeMode,
                paletteStyle = uiState.paletteStyle,
                useDynamicColor = uiState.useDynamicColor,
                useMiuixMonet = uiState.useMiuixMonet,
                seedColor = uiState.seedColor
            ) {
                val modifier = Modifier.fillMaxSize()
                if (uiState.useMiuix) {
                    // MiuixSurface(modifier = modifier) { MiuixSettingsPage(preferredViewModel) }
                } else {
                    Material3Surface(modifier = modifier) { NavHost(terminalMode = uiState.terminalMode) }
                }
            }
        }
    }
}