package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.ui.calendar.LunarAppUI
import com.example.ui.calendar.MainViewModel
import com.example.ui.calendar.MainViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "lunar-database"
        ).build()
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(db, applicationContext)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fontScale by viewModel.fontSizeMultiplier.collectAsStateWithLifecycle()
            
            MyApplicationTheme {
                val currentDensity = androidx.compose.ui.platform.LocalDensity.current
                val newDensity = androidx.compose.ui.unit.Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * fontScale
                )
                
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalDensity provides newDensity
                ) {
                    val calendarPermissions = rememberMultiplePermissionsState(
                        permissions = listOf(
                            android.Manifest.permission.READ_CALENDAR,
                            android.Manifest.permission.WRITE_CALENDAR
                        )
                    )

                    if (!calendarPermissions.allPermissionsGranted) {
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            calendarPermissions.launchMultiplePermissionRequest()
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        LunarAppUI(viewModel)
                    }
                }
            }
        }
    }
}
