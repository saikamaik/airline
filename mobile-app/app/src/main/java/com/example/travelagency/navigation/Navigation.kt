package com.example.travelagency.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.travelagency.presentation.view.authScreens.signInScreen.SignInScreen
import com.example.travelagency.presentation.view.authScreens.signUpScreen.SignUpScreen
import com.example.travelagency.presentation.view.launchScreen.LaunchScreen
import com.example.travelagency.presentation.view.mainScreen.MainScreen
import com.example.travelagency.presentation.view.requestScreen.RequestScreen
import com.example.travelagency.presentation.view.tourInfoScreen.TourInfoScreen

@Composable
fun Navigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Launch.route
    ) {

        composable(Screen.Launch.route) {
            LaunchScreen(navController)
        }

        composable(Screen.SignIn.route) {
            SignInScreen(navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }

        composable(Screen.Home.route) {
            MainScreen(navController)
        }

        composable(
            Screen.TourInfo.route + "/{tourId}",
            arguments = listOf(navArgument("tourId") {
                type = NavType.LongType
            })
        ) {
            TourInfoScreen(navController)
        }

        composable(
            Screen.Request.route + "/{tourId}",
            arguments = listOf(navArgument("tourId") {
                type = NavType.LongType
            })
        ) {
            RequestScreen(navController)
        }

    }
}
