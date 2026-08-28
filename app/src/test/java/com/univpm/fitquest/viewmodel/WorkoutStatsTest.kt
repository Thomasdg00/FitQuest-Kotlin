package com.univpm.fitquest.viewmodel

import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WorkoutStatsTest {
    private val zoneId = ZoneId.of("UTC")
    @Test
    fun weeklyDistanceTotalsIncludeOnlyCompletedWorkoutsFromCurrentWeek() {
        val now = timestamp("2026-06-03", 12)
        val workouts = listOf(
            workout(Sport.Walking, "2026-06-01", 1_000.0, completed = true),
            workout(Sport.Walking, "2026-05-31", 9_000.0, completed = true),
            workout(Sport.Running, "2026-06-02", 2_500.0, completed = true),
            workout(Sport.Walking, "2026-06-03", 2_000.0, completed = true, hour = 12),
            workout(Sport.Walking, "2026-06-03", 8_000.0, completed = true, hour = 13),
            workout(Sport.Cycling, "2026-06-02", 8_000.0, completed = false),
        )

        val totals = weeklyDistanceMetersBySport(workouts, now, zoneId)

        assertEquals(3_000.0, totals.getValue(Sport.Walking), 0.0)
        assertEquals(2_500.0, totals.getValue(Sport.Running), 0.0)
        assertEquals(0.0, totals.getValue(Sport.Cycling), 0.0)
    }

    @Test
    fun monthlyTrendGroupsBoundariesAndFiltersInvalidWorkouts() {
        val now = timestamp("2026-06-30", 12)
        val workouts = listOf(
            workout(Sport.Walking, "2026-06-07", 1_000.0, completed = true),
            workout(Sport.Running, "2026-06-07", 2_000.0, completed = true),
            workout(Sport.Cycling, "2026-06-08", 4_000.0, completed = true),
            workout(Sport.Walking, "2026-06-14", 500.0, completed = true),
            workout(Sport.Running, "2026-06-15", 1_500.0, completed = true),
            workout(Sport.Cycling, "2026-06-28", 2_800.0, completed = true),
            workout(Sport.Walking, "2026-06-29", 2_900.0, completed = true),
            workout(Sport.Running, "2026-06-30", 100.0, completed = true),
            workout(Sport.Cycling, "2026-06-01", 99_000.0, completed = false),
            workout(Sport.Walking, "2026-05-31", 99_000.0, completed = true),
            workout(Sport.Walking, "2026-06-30", 99_000.0, completed = true, hour = 13),
            workout(Sport.Walking, "2026-07-01", 99_000.0, completed = true),
        )

        val trend = monthlyDistanceTrend(workouts, now, zoneId)

        assertEquals(listOf("1–7", "8–14", "15–21", "22–28", "29–30"), trend.map { it.label })
        assertEquals(listOf(3.0, 4.5, 1.5, 2.8, 3.0), trend.map { it.distanceKm })
    }

    @Test
    fun monthlyTrendIncludesCurrentZeroSegmentAndOmitsFutureSegments() {
        val trend = monthlyDistanceTrend(
            workouts = listOf(
                workout(Sport.Walking, "2026-06-01", 1_000.0, completed = true),
                workout(Sport.Running, "2026-06-29", 9_000.0, completed = true),
            ),
            nowMillis = timestamp("2026-06-23", 12),
            zoneId = zoneId,
        )

        assertEquals(listOf("1–7", "8–14", "15–21", "22–28"), trend.map { it.label })
        assertEquals(listOf(1.0, 0.0, 0.0, 0.0), trend.map { it.distanceKm })
    }

    @Test
    fun monthlyTrendFinalSegmentUsesActualMonthEnd() {
        val trend = monthlyDistanceTrend(emptyList(), timestamp("2026-07-29", 12), zoneId)

        assertEquals("29–31", trend.last().label)
    }

    private fun workout(
        sport: Sport,
        date: String,
        distanceMeters: Double,
        completed: Boolean,
        hour: Int = 9,
    ) = WorkoutEntity(
        sport = sport.routeValue,
        startedAtMillis = timestamp(date, hour),
        distanceMeters = distanceMeters,
        isCompleted = completed,
    )

    private fun timestamp(date: String, hour: Int): Long = LocalDate.parse(date)
        .atTime(hour, 0)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}
