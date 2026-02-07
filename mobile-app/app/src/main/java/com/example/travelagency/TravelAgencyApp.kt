package com.example.travelagency

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelAgencyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("TravelAgencyApp", "========================================")
        Log.e("TravelAgencyApp", "APPLICATION STARTED!")
        Log.e("TravelAgencyApp", "========================================")
    }
}
