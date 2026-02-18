package com.example.travelagency

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TravelAgencyApp : Application(), ImageLoaderFactory {
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    override fun onCreate() {
        super.onCreate()
        Log.e("TravelAgencyApp", "========================================")
        Log.e("TravelAgencyApp", "APPLICATION STARTED!")
        Log.e("TravelAgencyApp", "========================================")
    }
    
    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
}
