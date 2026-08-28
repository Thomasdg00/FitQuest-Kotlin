package com.univpm.fitquest.tracking.service

import android.app.Notification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackingNotificationHelperTest {
    @Test
    fun failedSaveNotificationHasItalianDistinctRetryAndDiscardActions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notification = TrackingNotificationHelper(context).buildNotification(
            TrackingServiceState(
                lifecycleState = TrackingLifecycleState.SaveFailed,
                sport = Sport.Running,
            ),
        )

        assertEquals("Tracking FitQuest attivo", notification.extras.getCharSequence(Notification.EXTRA_TITLE))
        assertEquals(2, notification.actions.size)
        assertEquals(context.getString(R.string.track_retry_save), notification.actions[0].title)
        assertEquals(context.getString(R.string.track_discard_workout), notification.actions[1].title)
        assertNotEquals(notification.actions[0].actionIntent, notification.actions[1].actionIntent)
    }
}
