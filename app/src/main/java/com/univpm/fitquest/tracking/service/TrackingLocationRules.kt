package com.univpm.fitquest.tracking.service

const val MAX_ACCEPTED_ACCURACY_METERS_TRACKING = 30f
const val MAX_ACCEPTED_ACCURACY_METERS_INITIAL = 500f

fun isUsableLocationSample(
    latitude: Double,
    longitude: Double,
    accuracyMeters: Float?,
    isInitialFix: Boolean = false,
): Boolean {
    val validCoordinates = latitude in -90.0..90.0 && longitude in -180.0..180.0
    val maxAccuracy = if (isInitialFix) MAX_ACCEPTED_ACCURACY_METERS_INITIAL else MAX_ACCEPTED_ACCURACY_METERS_TRACKING
    val accurateEnough = accuracyMeters == null || accuracyMeters <= maxAccuracy
    return validCoordinates && accurateEnough
}
