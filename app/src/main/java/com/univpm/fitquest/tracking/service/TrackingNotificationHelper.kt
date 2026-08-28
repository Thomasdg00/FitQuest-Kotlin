package com.univpm.fitquest.tracking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.resources.getSportName

class TrackingNotificationHelper(private val context: Context) {
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_tracking),
            NotificationManager.IMPORTANCE_LOW,
        )
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(state: TrackingServiceState): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(context.getString(R.string.notification_tracking_active))
            .setContentText(notificationText(state))
            .setOngoing(state.lifecycleState != TrackingLifecycleState.Idle)
            .setOnlyAlertOnce(true)

        when (state.lifecycleState) {
            TrackingLifecycleState.Running -> {
                builder.addAction(
                    notificationAction(
                        TrackingService.ACTION_PAUSE,
                        context.getString(R.string.pause),
                        REQUEST_PAUSE,
                        android.R.drawable.ic_media_pause,
                    ),
                )
                builder.addAction(stopAction())
            }
            TrackingLifecycleState.Paused -> {
                builder.addAction(
                    notificationAction(
                        TrackingService.ACTION_RESUME,
                        context.getString(R.string.resume),
                        REQUEST_RESUME,
                        android.R.drawable.ic_media_play,
                    ),
                )
                builder.addAction(stopAction())
            }
            TrackingLifecycleState.SaveFailed -> {
                builder.addAction(
                    notificationAction(
                        TrackingService.ACTION_RETRY_SAVE,
                        context.getString(R.string.track_retry_save),
                        REQUEST_RETRY_SAVE,
                        android.R.drawable.ic_popup_sync,
                    ),
                )
                builder.addAction(
                    notificationAction(
                        TrackingService.ACTION_DISCARD_FAILED_SAVE,
                        context.getString(R.string.track_discard_workout),
                        REQUEST_DISCARD_FAILED_SAVE,
                        android.R.drawable.ic_menu_delete,
                    ),
                )
            }
            TrackingLifecycleState.Stopping,
            TrackingLifecycleState.Idle,
            -> Unit
        }

        return builder.build()
    }

    private fun notificationText(state: TrackingServiceState): String {
        val sportText = state.sport?.let { context.getSportName(it) }
            ?: context.getString(R.string.notification_workout)
        return when (state.lifecycleState) {
            TrackingLifecycleState.Running -> context.getString(R.string.notification_sport_active, sportText)
            TrackingLifecycleState.Paused -> context.getString(R.string.notification_sport_paused, sportText)
            TrackingLifecycleState.Stopping -> context.getString(R.string.notification_sport_saving, sportText)
            TrackingLifecycleState.SaveFailed -> context.getString(R.string.notification_sport_save_failed, sportText)
            TrackingLifecycleState.Idle -> sportText
        }
    }

    private fun stopAction(): NotificationCompat.Action = notificationAction(
        TrackingService.ACTION_STOP,
        context.getString(R.string.stop),
        REQUEST_STOP,
        android.R.drawable.ic_menu_close_clear_cancel,
    )

    private fun notificationAction(
        action: String,
        title: String,
        requestCode: Int,
        icon: Int,
    ): NotificationCompat.Action {
        val intent = Intent(context, TrackingService::class.java)
            .setAction(action)
        val pendingIntent = PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    private companion object {
        const val CHANNEL_ID = "tracking"
        const val REQUEST_PAUSE = 2001
        const val REQUEST_RESUME = 2002
        const val REQUEST_STOP = 2003
        const val REQUEST_RETRY_SAVE = 2004
        const val REQUEST_DISCARD_FAILED_SAVE = 2005
    }
}
