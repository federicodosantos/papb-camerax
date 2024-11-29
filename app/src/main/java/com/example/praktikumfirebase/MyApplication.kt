package com.example.praktikumfirebase

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Firebase
        FirebaseApp.initializeApp(this)

        // Inisialisasi App Check
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}