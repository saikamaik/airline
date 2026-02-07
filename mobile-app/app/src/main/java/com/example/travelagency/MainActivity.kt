package com.example.travelagency

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.travelagency.navigation.Navigation
import com.example.travelagency.ui.theme.TravelAgencyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("MainActivity", "========================================")
        Log.e("MainActivity", "MAIN ACTIVITY CREATED!")
        Log.e("MainActivity", "========================================")
        
        setContent {
            Log.e("MainActivity", "setContent called")
            TravelAgencyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Log.e("MainActivity", "Navigation starting...")
                    Navigation(navController = navController)
                }
            }
        }
    }
}
