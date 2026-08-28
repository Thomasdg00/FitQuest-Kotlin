package com.univpm.fitquest.viewmodel

import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.domain.model.Sport
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

fun weeklyDistanceMetersBySport(
    workouts: List<WorkoutEntity>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): Map<Sport, Double> {
    val weekStartMillis = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .toLocalDate()
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

    return Sport.entries.associateWith { sport ->
        workouts
            .filter { it.isCompleted }
            .filter { it.sport == sport.routeValue }
            .filter { it.startedAtMillis in weekStartMillis..nowMillis }
            .sumOf { it.distanceMeters }
    }
}

fun metersToKm(meters: Double): Double = meters / 1_000.0

fun kmToMeters(km: Double): Double = km * 1_000.0

fun defaultWeeklyGoalMeters(sport: Sport): Double {
    return when (sport) {
        Sport.Walking -> 10_000.0
        Sport.Running -> 15_000.0
        Sport.Cycling -> 30_000.0
    }
}
