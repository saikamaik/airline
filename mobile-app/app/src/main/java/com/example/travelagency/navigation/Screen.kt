package com.example.travelagency.navigation

sealed class Screen(
    val route: String,
    val iconActive: Int?,
    val iconInactive: Int?
) {
    object Launch : Screen(
        route = "launch_screen",
        null, null
    )

    object SignIn : Screen(
        route = "sign_in_screen",
        null, null
    )

    object SignUp : Screen(
        route = "sign_up_screen",
        null, null
    )

    object Home : Screen(
        route = "home_screen",
        iconActive = null,
        iconInactive = null
    )

    object TourInfo : Screen(
        route = "tour_info_screen",
        null, null
    )

    object Request : Screen(
        route = "request_screen",
        null, null
    )

    object MyRequests : Screen(
        route = "my_requests_screen",
        null, null
    )

    object Profile : Screen(
        route = "profile_screen",
        null, null
    )

    object Favorites : Screen(
        route = "favorites_screen",
        null, null
    )
}
