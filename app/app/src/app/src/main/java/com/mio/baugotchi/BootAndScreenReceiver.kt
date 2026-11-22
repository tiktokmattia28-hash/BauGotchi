package com.mio.baugotchi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import java.util.Calendar

class BootAndScreenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val totalMillisToday = getTodayScreenTimeMillis(context)
        val fourAndHalfHoursMillis = (4.5 * 60 * 60 * 1000).toLong()

        if (totalMillisToday >= fourAndHalfHoursMillis) {
            val hours = String.format("%.1f", totalMillisToday / (1000.0 * 60 * 60))
            sendBarkAlert(
                context,
                "BAU! BAU! Ma sei coglione?!",
                "Spengi sto cazzo di Redmi! Lo usi da $hours ore oggi, riposati!"
            )
        }
    }

    private fun getTodayScreenTimeMillis(context: Context): Long {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cal.timeInMillis,
            System.currentTimeMillis()
        )
        var totalTime = 0L
        stats?.forEach { totalTime += it.totalTimeInForeground }
        return totalTime
    }

    private fun sendBarkAlert(context: Context, title: String, message: String) {
        val channelId = "baugotchi_bark"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Insulti Cane", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(101, notification)

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 400), -1))
        }
    }
}
