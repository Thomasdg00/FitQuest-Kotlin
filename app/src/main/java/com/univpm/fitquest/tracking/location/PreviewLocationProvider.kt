package com.univpm.fitquest.tracking.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class PreviewLocation(
    val latitude: Double,
    val longitude: Double,
)

class FusedPreviewLocationProvider(
    private val fusedLocationClient: FusedLocationProviderClient,
) {
    @SuppressLint("MissingPermission")
    suspend fun currentPreviewLocation(): PreviewLocation? {
        return runCatching {
            val freshLocation = withTimeoutOrNull(FRESH_LOCATION_TIMEOUT_MILLIS) {
                fusedLocationClient.awaitCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY)
            }
            freshLocation?.toPreviewLocation()
                ?: fusedLocationClient.awaitLastLocation()?.toPreviewLocation()
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    private suspend fun FusedLocationProviderClient.awaitCurrentLocation(priority: Int): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
                .addOnCanceledListener {
                    if (continuation.isActive) continuation.resume(null)
                }
            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
        }

    @SuppressLint("MissingPermission")
    private suspend fun FusedLocationProviderClient.awaitLastLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            lastLocation
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
                .addOnCanceledListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }

    private fun Location.toPreviewLocation(): PreviewLocation {
        return PreviewLocation(
            latitude = latitude,
            longitude = longitude,
        )
    }

    private companion object {
        const val FRESH_LOCATION_TIMEOUT_MILLIS = 4_000L
    }
}
