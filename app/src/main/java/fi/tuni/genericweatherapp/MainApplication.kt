package fi.tuni.genericweatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Global application state, used by Hilt for di service management
@HiltAndroidApp
class MainApplication : Application()
