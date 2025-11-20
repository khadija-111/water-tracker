package com.example.projetn2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Vérifier la permission
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            // Messages aléatoires pour varier
            val messages = arrayOf(
                "💧 N'oubliez pas de boire de l'eau!",
                "🌊 Il est temps de s'hydrater!",
                "💦 Une petite pause hydratation?",
                "🥤 Restez hydraté, restez en forme!",
                "💙 Votre corps a besoin d'eau!"
            )

            val randomMessage = messages.random()

            // Créer la notification
            val notification = NotificationCompat.Builder(context, "auth_channel")
                .setContentTitle("⏰ Rappel hydratation")
                .setContentText(randomMessage)
                .setSmallIcon(R.drawable.outline_check_small_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            // Afficher la notification
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}