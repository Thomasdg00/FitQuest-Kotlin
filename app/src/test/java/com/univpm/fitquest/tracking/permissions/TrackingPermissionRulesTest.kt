package com.univpm.fitquest.tracking.permissions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingPermissionRulesTest {
    @Test
    fun notificationPermissionIsRequiredFromAndroid13() {
        assertFalse(isNotificationPermissionRequired(32))
        assertTrue(isNotificationPermissionRequired(33))
    }
}
