package com.univpm.fitquest.viewmodel

import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyGoalsTest {
    @Test
    fun defaultWeeklyGoalMetersMatchesExpectedTargets() {
        assertEquals(10_000.0, defaultWeeklyGoalMeters(Sport.Walking), 0.0)
        assertEquals(15_000.0, defaultWeeklyGoalMeters(Sport.Running), 0.0)
        assertEquals(30_000.0, defaultWeeklyGoalMeters(Sport.Cycling), 0.0)
    }
}
