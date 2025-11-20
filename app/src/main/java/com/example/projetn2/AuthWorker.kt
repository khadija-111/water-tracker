package com.example.projetn2

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class AuthWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Vérifier la permission de notification
            val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    applicationContext.checkSelfPermission(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

            if (permissionGranted) {
                // Créer et afficher la notification
                val notification = NotificationCompat.Builder(applicationContext, "auth_channel")
                    .setContentTitle("💧 Rappel hydratation")
                    .setContentText("N'oubliez pas de boire de l'eau!")
                    .setSmallIcon(R.drawable.outline_check_small_24)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                NotificationManagerCompat.from(applicationContext)
                    .notify(2001, notification)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}