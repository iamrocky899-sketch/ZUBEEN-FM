package com.amairatech.zubeenfm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amairatech.zubeenfm.ui.MainAppScreen
import com.amairatech.zubeenfm.ui.RadioViewModel
import com.amairatech.zubeenfm.ui.theme.ZubeenFMTheme

class MainActivity : ComponentActivity() {

    private val radioViewModel: RadioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        setContent {
            ZubeenFMTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainAppScreen(radioViewModel = radioViewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        radioViewModel.onAppForegroundChanged(true)
    }

    override fun onStop() {
        super.onStop()
        radioViewModel.onAppForegroundChanged(false)
    }
}