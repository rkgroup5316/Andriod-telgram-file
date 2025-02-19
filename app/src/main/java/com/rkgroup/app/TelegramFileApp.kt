package com.rkgroup.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import androidx.work.Configuration
import androidx.work.WorkManager
import javax.inject.Inject

@HiltAndroidApp
class TelegramFileApp : Application(), Configuration.Provider {

    companion object {
        const val UPLOAD_NOTIFICATION_CHANNEL_ID = "file_upload_channel"
        const val UPLOAD_NOTIFICATION_CHANNEL_NAME = "File Upload Status"
    }

    @Inject
    lateinit var workerConfiguration: Configuration

    override fun onCreate() {
        super.onCreate()
        initializeNotificationChannels()
        initializeWorkManager()
    }

    private fun initializeNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = 
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create upload status notification channel
            NotificationChannel(
                UPLOAD_NOTIFICATION_CHANNEL_ID,
                UPLOAD_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows file upload progress and status"
                setShowBadge(false)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    private fun initializeWorkManager() {
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        )
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}
