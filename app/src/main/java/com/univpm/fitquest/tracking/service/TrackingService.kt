package com.univpm.fitquest.tracking.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.univpm.fitquest.FitQuestApplication
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.calories.MetCalorieCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackingService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationHelper: TrackingNotificationHelper
    private val appContainer by lazy {
        (application as FitQuestApplication).appContainer
    }
    private val workoutRepository by lazy {
        appContainer.workoutRepository
    }
    private val workoutSaveCoordinator by lazy {
        WorkoutSaveCoordinator(workoutRepository)
    }
    private val userSettingsRepository by lazy {
        appContainer.userSettingsRepository
    }
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)
    private val routePoints = mutableListOf<InMemoryRoutePoint>()
    private val sessionStateMachine = TrackingSessionStateMachine()
    private val locationGuard = TrackingLocationGuard()

    private var currentState = TrackingServiceState()
    private var sessionStartedAtMillis: Long? = null
    private var lastAcceptedLocation: Location? = null
    private var activeStartedAtElapsedRealtime: Long? = null
    private var accumulatedElapsedMillis: Long = 0L
    private var elapsedTicker: Job? = null
    private var bodyWeightKg: Double = DEFAULT_BODY_WEIGHT_KG
    private var elevationGainMeters: Double = 0.0
    private var elevationLossMeters: Double = 0.0
    private var saveJob: Job? = null
    private var activeOneShotLocationRequest: CancellationTokenSource? = null
    private var oneShotLocationTimeout: Job? = null
    private var activeLocationCallback: LocationCallback? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationHelper = TrackingNotificationHelper(applicationContext)
        notificationHelper.createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking(intent)
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
            ACTION_RETRY_SAVE -> retrySave()
            ACTION_DISCARD_FAILED_SAVE -> discardFailedSave()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        locationGuard.invalidateSession()
        stopLocationUpdates()
        elapsedTicker?.cancel()
        serviceJob.cancel()
        if (currentState.errorMessage == null) {
            TrackingServiceController.updateState(TrackingServiceState())
        }
        super.onDestroy()
    }

    private fun startTracking(intent: Intent) {
        if (!sessionStateMachine.beginStart()) return
        locationGuard.onStart()
        stopLocationUpdates()

        if (!canStartForegroundTracking()) {
            stopWithError(getString(R.string.tracking_error_permissions_required))
            return
        }

        routePoints.clear()
        sessionStartedAtMillis = System.currentTimeMillis()
        lastAcceptedLocation = null
        accumulatedElapsedMillis = 0L
        activeStartedAtElapsedRealtime = SystemClock.elapsedRealtime()
        bodyWeightKg = DEFAULT_BODY_WEIGHT_KG
        resetElevationMetrics()

        val sport = Sport.fromRouteValue(intent.getStringExtra(EXTRA_SPORT))
        currentState = TrackingServiceState(
            lifecycleState = sessionStateMachine.lifecycleState,
            sport = sport,
        )
        TrackingServiceController.updateState(currentState)
        // Android requires a foreground service and ongoing notification for long-running location work.
        startForeground(NOTIFICATION_ID, notificationHelper.buildNotification(currentState))
        loadBodyWeight()
        startElapsedTicker()
        startLocationUpdates()
    }

    private fun pauseTracking() {
        if (!sessionStateMachine.pause()) return
        locationGuard.invalidateSession()

        activeStartedAtElapsedRealtime?.let { startedAt ->
            accumulatedElapsedMillis += SystemClock.elapsedRealtime() - startedAt
        }
        activeStartedAtElapsedRealtime = null
        stopLocationUpdates()
        currentState = currentState.copy(
            lifecycleState = sessionStateMachine.lifecycleState,
            elapsedMillis = accumulatedElapsedMillis,
        )
        publishState(updateNotification = true)
    }

    private fun resumeTracking() {
        if (sessionStateMachine.lifecycleState != TrackingLifecycleState.Paused) return
        if (!hasForegroundLocationPermission()) {
            stopWithError(getString(R.string.tracking_error_location_missing))
            return
        }
        if (!sessionStateMachine.resume()) return
        locationGuard.invalidateSession()

        activeStartedAtElapsedRealtime = SystemClock.elapsedRealtime()
        currentState = currentState.copy(
            lifecycleState = sessionStateMachine.lifecycleState,
            errorMessage = null,
        )
        publishState(updateNotification = true)
        startElapsedTicker()
        startLocationUpdates()
    }

    private fun stopTracking() {
        when (sessionStateMachine.lifecycleState) {
            TrackingLifecycleState.Idle -> {
                stopSelf()
                return
            }
            TrackingLifecycleState.Running,
            TrackingLifecycleState.Paused,
            -> Unit
            TrackingLifecycleState.Stopping,
            TrackingLifecycleState.SaveFailed,
            -> return
        }

        val sport = currentState.sport
        if (sport == null) {
            stopWithError(getString(R.string.tracking_error_missing_start_data))
            return
        }
        val startedAtMillis = sessionStartedAtMillis
        if (startedAtMillis == null) {
            stopWithError(getString(R.string.tracking_error_missing_start_data))
            return
        }
        val durationMillis = currentElapsedMillis()
        if (durationMillis <= 0L) {
            stopWithError(getString(R.string.tracking_error_too_short))
            return
        }

        val request = CompletedWorkoutSaveRequest(
            sport = sport,
            startedAtMillis = startedAtMillis,
            endedAtMillis = System.currentTimeMillis(),
            durationMillis = durationMillis,
            distanceMeters = currentState.distanceMeters,
            routeSnapshot = routePoints.toList(),
            caloriesKcal = MetCalorieCalculator.estimateKcal(
                sport = sport,
                activeDurationMillis = durationMillis,
                distanceMeters = currentState.distanceMeters,
                bodyWeightKg = bodyWeightKg,
            ),
            elevationGainMeters = currentState.elevationGainMeters,
            elevationLossMeters = currentState.elevationLossMeters,
        )
        if (!sessionStateMachine.beginStop(request)) return
        locationGuard.invalidateSession()

        stopLocationUpdates()
        elapsedTicker?.cancel()
        currentState = currentState.copy(
            lifecycleState = sessionStateMachine.lifecycleState,
            elapsedMillis = durationMillis,
        )
        publishState(updateNotification = true)
        launchSave(request)
    }

    private fun retrySave() {
        if (saveJob?.isActive == true) return

        val request = sessionStateMachine.beginRetry() ?: return
        currentState = currentState.copy(
            lifecycleState = sessionStateMachine.lifecycleState,
            errorMessage = null,
        )
        publishState(updateNotification = true)
        launchSave(request)
    }

    private fun discardFailedSave() {
        if (!sessionStateMachine.beginDiscardFailedSave()) return

        locationGuard.invalidateSession()
        stopLocationUpdates()
        elapsedTicker?.cancel()
        clearSession()
        currentState = TrackingServiceState()
        TrackingServiceController.updateState(currentState)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun launchSave(request: CompletedWorkoutSaveRequest) {
        if (saveJob?.isActive == true) return

        saveJob = serviceScope.launch {
            try {
                workoutSaveCoordinator.saveCompletedWorkout(request)
                if (!sessionStateMachine.onSaveSucceeded()) return@launch

                clearSession()
                currentState = TrackingServiceState()
                TrackingServiceController.updateState(currentState)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (!sessionStateMachine.onSaveFailed()) return@launch

                currentState = currentState.copy(
                    lifecycleState = sessionStateMachine.lifecycleState,
                    errorMessage = getString(R.string.tracking_error_save_failed),
                )
                publishState(updateNotification = true)
            }
        }
    }

    private fun stopWithError(message: String) {
        locationGuard.invalidateSession()
        stopLocationUpdates()
        elapsedTicker?.cancel()
        sessionStateMachine.reset()
        clearSession()
        currentState = TrackingServiceState(errorMessage = message)
        TrackingServiceController.updateState(currentState)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun clearSession() {
        routePoints.clear()
        sessionStartedAtMillis = null
        lastAcceptedLocation = null
        activeStartedAtElapsedRealtime = null
        accumulatedElapsedMillis = 0L
        resetElevationMetrics()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasForegroundLocationPermission()) {
            stopWithError(getString(R.string.tracking_error_location_missing))
            return
        }

        val generation = locationGuard.generation
        fetchInitialLocation(generation)
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    handleLocation(location, generation)
                }
            }
        }
        activeLocationCallback = callback

        fusedLocationClient.requestLocationUpdates(
            trackingLocationRequest(),
            callback,
            Looper.getMainLooper(),
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchInitialLocation(generation: Long) {
        if (!locationGuard.allowsCallback(generation, sessionStateMachine.lifecycleState)) return
        val cts = beginOneShotLocationRequest()

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (!finishOneShotLocationRequest(cts)) return@addOnSuccessListener
                if (!locationGuard.allowsCallback(generation, sessionStateMachine.lifecycleState)) {
                    return@addOnSuccessListener
                }
                if (location != null) {
                    handleLocation(location, generation)
                } else {
                    fetchNetworkLocationFallback(generation)
                }
            }
            .addOnFailureListener {
                if (finishOneShotLocationRequest(cts)) {
                    fetchNetworkLocationFallback(generation)
                }
            }
            .addOnCanceledListener {
                if (finishOneShotLocationRequest(cts)) {
                    fetchNetworkLocationFallback(generation)
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun fetchNetworkLocationFallback(generation: Long) {
        if (!locationGuard.allowsCallback(generation, sessionStateMachine.lifecycleState)) return
        val cts = beginOneShotLocationRequest()

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (!finishOneShotLocationRequest(cts)) return@addOnSuccessListener
                if (!locationGuard.allowsCallback(generation, sessionStateMachine.lifecycleState)) {
                    return@addOnSuccessListener
                }
                if (location != null) {
                    handleLocation(location, generation)
                } else {
                    fallbackLastLocation(generation)
                }
            }
            .addOnFailureListener {
                if (finishOneShotLocationRequest(cts)) {
                    fallbackLastLocation(generation)
                }
            }
            .addOnCanceledListener {
                if (finishOneShotLocationRequest(cts)) {
                    fallbackLastLocation(generation)
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackLastLocation(generation: Long) {
        if (!locationGuard.allowsCallback(generation, sessionStateMachine.lifecycleState)) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (
                location != null &&
                locationGuard.allowsCallback(generation, sessionStateMachine.lifecycleState) &&
                locationGuard.isCachedLocationFresh(
                    elapsedRealtimeNanos = location.elapsedRealtimeNanos,
                    nowElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos(),
                )
            ) {
                handleLocation(location, generation)
            }
        }
    }

    private fun stopLocationUpdates() {
        cancelActiveOneShotLocationRequest()
        if (::fusedLocationClient.isInitialized) {
            activeLocationCallback?.let(fusedLocationClient::removeLocationUpdates)
        }
        activeLocationCallback = null
    }

    private fun beginOneShotLocationRequest(): CancellationTokenSource {
        cancelActiveOneShotLocationRequest()
        val cts = CancellationTokenSource()
        activeOneShotLocationRequest = cts
        oneShotLocationTimeout = serviceScope.launch {
            delay(ONE_SHOT_LOCATION_TIMEOUT_MS)
            if (activeOneShotLocationRequest === cts) {
                cts.cancel()
            }
        }
        return cts
    }

    private fun finishOneShotLocationRequest(cts: CancellationTokenSource): Boolean {
        if (activeOneShotLocationRequest !== cts) return false

        activeOneShotLocationRequest = null
        oneShotLocationTimeout?.cancel()
        oneShotLocationTimeout = null
        return true
    }

    private fun cancelActiveOneShotLocationRequest() {
        val request = activeOneShotLocationRequest
        activeOneShotLocationRequest = null
        oneShotLocationTimeout?.cancel()
        oneShotLocationTimeout = null
        request?.cancel()
    }

    private fun handleLocation(location: Location, generation: Long) {
        if (
            !locationGuard.canAcceptSample(
                capturedGeneration = generation,
                lifecycleState = sessionStateMachine.lifecycleState,
                elapsedRealtimeNanos = location.elapsedRealtimeNanos,
            )
        ) {
            return
        }
        if (!isUsableLocation(location)) return

        val lastLocation = lastAcceptedLocation.takeIf {
            locationGuard.hasBaseline
        }
        val distanceDelta = lastLocation?.distanceTo(location)?.toDouble() ?: 0.0
        val altitudeMeters = if (location.hasAltitude()) location.altitude else null
        val speed = when {
            location.hasSpeed() -> location.speed
            lastLocation != null -> calculatedSpeed(lastLocation, location, distanceDelta)
            else -> null
        }

        if (lastLocation != null && isImplausibleJump(lastLocation, location, distanceDelta)) return

        val acceptedSample = locationGuard.acceptSample(
            capturedGeneration = generation,
            lifecycleState = sessionStateMachine.lifecycleState,
            elapsedRealtimeNanos = location.elapsedRealtimeNanos,
        ) ?: return
        lastAcceptedLocation = location
        routePoints += InMemoryRoutePoint(
            latitude = location.latitude,
            longitude = location.longitude,
            recordedAtMillis = location.time,
            altitudeMeters = altitudeMeters,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
            speedMetersPerSecond = speed,
        )


        val prevAlt = currentState.currentAltitudeMeters.takeIf {
            lastLocation != null
        }
        if (altitudeMeters != null && prevAlt != null) {
            val delta = altitudeMeters - prevAlt
            if (kotlin.math.abs(delta) >= MIN_ALTITUDE_DELTA_METERS) {
                if (delta > 0.0) elevationGainMeters += delta
                else elevationLossMeters += -delta
            }
        }

        val acceptedDistanceDelta = if (acceptedSample == AcceptedLocationSample.Movement) {
            distanceDelta
        } else {
            0.0
        }
        val updatedDistanceMeters = currentState.distanceMeters + acceptedDistanceDelta
        val updatedElapsedMillis = currentElapsedMillis()
        currentState = currentState.copy(
            elapsedMillis = updatedElapsedMillis,
            distanceMeters = updatedDistanceMeters,
            latestLatitude = location.latitude,
            latestLongitude = location.longitude,
            currentSpeedMetersPerSecond = speed,
            currentAltitudeMeters = altitudeMeters,
            routePoints = routePoints.toList(),
            elevationGainMeters = elevationGainMeters,
            elevationLossMeters = elevationLossMeters,
            estimatedCaloriesKcal = estimateCalories(
                durationMillis = updatedElapsedMillis,
                distanceMeters = updatedDistanceMeters,
            ),
            errorMessage = null,
        )
        publishState(updateNotification = false)
    }

    private fun isUsableLocation(location: Location): Boolean {
        return isUsableLocationSample(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
            isInitialFix = !locationGuard.hasBaseline,
        )
    }

    private fun isImplausibleJump(
        previous: Location,
        current: Location,
        distanceDelta: Double,
    ): Boolean {
        val elapsedSeconds = (current.elapsedRealtimeNanos - previous.elapsedRealtimeNanos) / 1_000_000_000.0
        if (elapsedSeconds <= 0.0) return true
        return distanceDelta / elapsedSeconds > MAX_REASONABLE_SPEED_METERS_PER_SECOND
    }

    private fun calculatedSpeed(
        previous: Location,
        current: Location,
        distanceDelta: Double,
    ): Float? {
        val elapsedSeconds = (current.elapsedRealtimeNanos - previous.elapsedRealtimeNanos) / 1_000_000_000.0
        if (elapsedSeconds <= 0.0) return null
        return (distanceDelta / elapsedSeconds).toFloat()
    }

    private fun loadBodyWeight() {
        serviceScope.launch {
            bodyWeightKg = runCatching {
                withContext(Dispatchers.IO) {
                    userSettingsRepository.getSettings()?.bodyWeightKg
                        ?.takeIf { it.isFinite() && it in 20.0..300.0 }
                        ?: DEFAULT_BODY_WEIGHT_KG
                }
            }.getOrDefault(DEFAULT_BODY_WEIGHT_KG)
        }
    }

    private fun resetElevationMetrics() {
        elevationGainMeters = 0.0
        elevationLossMeters = 0.0
    }

    private fun estimateCalories(
        durationMillis: Long,
        distanceMeters: Double,
    ): Double {
        val sport = currentState.sport ?: return 0.0
        return MetCalorieCalculator.estimateKcal(
            sport = sport,
            activeDurationMillis = durationMillis,
            distanceMeters = distanceMeters,
            bodyWeightKg = bodyWeightKg,
        )
    }

    private fun startElapsedTicker() {
        elapsedTicker?.cancel()
        elapsedTicker = serviceScope.launch {
            while (isActive && sessionStateMachine.lifecycleState == TrackingLifecycleState.Running) {
                val elapsedMillis = currentElapsedMillis()
                currentState = currentState.copy(
                    elapsedMillis = elapsedMillis,
                    estimatedCaloriesKcal = estimateCalories(
                        durationMillis = elapsedMillis,
                        distanceMeters = currentState.distanceMeters,
                    ),
                )
                publishState(updateNotification = false)
                delay(1_000)
            }
        }
    }

    private fun currentElapsedMillis(): Long {
        val activeMillis = activeStartedAtElapsedRealtime?.let { startedAt ->
            SystemClock.elapsedRealtime() - startedAt
        } ?: 0L
        return accumulatedElapsedMillis + activeMillis
    }

    private fun publishState(updateNotification: Boolean) {
        TrackingServiceController.updateState(currentState)
        if (updateNotification && currentState.lifecycleState != TrackingLifecycleState.Idle) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notificationHelper.buildNotification(currentState))
        }
    }

    private fun canStartForegroundTracking(): Boolean {
        val hasNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        return hasForegroundLocationPermission() && hasNotifications
    }

    private fun hasForegroundLocationPermission(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun trackingLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MILLIS)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()
    }

    private fun Context.hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ACTION_START = "com.univpm.fitquest.tracking.action.START"
        const val ACTION_PAUSE = "com.univpm.fitquest.tracking.action.PAUSE"
        const val ACTION_RESUME = "com.univpm.fitquest.tracking.action.RESUME"
        const val ACTION_STOP = "com.univpm.fitquest.tracking.action.STOP"
        const val ACTION_RETRY_SAVE = "com.univpm.fitquest.tracking.action.RETRY_SAVE"
        const val ACTION_DISCARD_FAILED_SAVE = "com.univpm.fitquest.tracking.action.DISCARD_FAILED_SAVE"
        const val EXTRA_SPORT = "sport"

        private const val NOTIFICATION_ID = 1001
        private const val LOCATION_INTERVAL_MILLIS = 5_000L
        private const val LOCATION_FASTEST_INTERVAL_MILLIS = 2_000L
        private const val ONE_SHOT_LOCATION_TIMEOUT_MS = 5_000L
        private const val MIN_DISTANCE_METERS = 3f
        private const val MAX_REASONABLE_SPEED_METERS_PER_SECOND = 80.0
        private const val MIN_ALTITUDE_DELTA_METERS = 1.0
        private const val DEFAULT_BODY_WEIGHT_KG = 70.0
    }
}
