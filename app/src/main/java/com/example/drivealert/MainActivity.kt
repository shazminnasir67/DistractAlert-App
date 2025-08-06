package com.example.drivealert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.drivealert.ui.navigation.DriveAlertNavGraph
import com.example.drivealert.ui.theme.DriveAlertTheme
import com.example.drivealert.ui.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DriveAlertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val sharedViewModel: SharedViewModel = viewModel()
                    
                    // Set context in SharedViewModel
                    sharedViewModel.setContext(this@MainActivity)
                    
                    DriveAlertNavGraph(
                        navController = navController,
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }
    }
}
