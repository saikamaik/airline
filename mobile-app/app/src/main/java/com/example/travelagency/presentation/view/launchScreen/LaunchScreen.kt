package com.example.travelagency.presentation.view.launchScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.travelagency.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun LaunchScreen(
    navHostController: NavHostController
) {
    val viewModel: LaunchViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        delay(1500)
        if (uiState.value.isLoggedIn) {
            navHostController.navigate(Screen.Home.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
            }
        } else {
            navHostController.navigate(Screen.SignIn.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Flight,
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TravelAgency",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Путешествуй с нами",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}
