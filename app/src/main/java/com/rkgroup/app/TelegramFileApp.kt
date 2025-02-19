package com.rkgroup.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Custom Application class for the Telegram File Sharing app.
 * Handles initialization of Hilt dependency injection, WorkManager, and notification channels.
 */
@HiltAndroidApp
class TelegramFileApp : Application(), Configuration.Provider {

    companion object {
        const val UPLOAD_CHANNEL_ID = "file_upload_channel"
        const val UPLOAD_CHANNEL_NAME = "File Upload Status"
        
        private lateinit var instance: TelegramFileApp
        
        fun getInstance(): TelegramFileApp = instance
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupNotificationChannels()
    }

    /**
     * Creates notification channels for Android O and above
     */
    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = 
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            NotificationChannel(
                UPLOAD_CHANNEL_ID,
                UPLOAD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the progress of file uploads to Telegram"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    /**
     * Provides WorkManager configuration with Hilt integration
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}
