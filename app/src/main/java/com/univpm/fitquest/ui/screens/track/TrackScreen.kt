package com.univpm.fitquest.ui.screens.track

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.location.FusedPreviewLocationProvider
import com.univpm.fitquest.tracking.permissions.isNotificationPermissionRequired
import com.univpm.fitquest.tracking.service.TrackingLifecycleState
import com.univpm.fitquest.viewmodel.TrackViewModel

@Composable
fun TrackScreen(
    initialSport: Sport = Sport.Walking,
    viewModel: TrackViewModel,
    previewLocationProvider: FusedPreviewLocationProvider,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var permissionState by remember {
        mutableStateOf(readTrackingPermissionState(context))
    }
    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        contract = RequestMultiplePermissions(),
    ) {
        permissionState = readTrackingPermissionState(context)
    }
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission(),
    ) {
        permissionState = readTrackingPermissionState(context)
    }
    val mapState by viewModel.mapState.collectAsState()
    val panelState by viewModel.panelState.collectAsState()

    val canStartTracking = permissionState.foregroundLocationGranted &&
        permissionState.notificationPermissionGranted

    var localSport by rememberSaveable(initialSport) { mutableStateOf(initialSport) }
    val activeSport = panelState.sport ?: localSport

    LaunchedEffect(permissionState.foregroundLocationGranted) {
        if (permissionState.foregroundLocationGranted) {
            viewModel.setInitialPreviewLocation(previewLocationProvider.currentPreviewLocation())
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LiveRouteMap(
            routePoints = mapState.routePoints,
            lifecycleState = mapState.lifecycleState,
            previewLocation = mapState.previewLocation,
            locationPermissionGranted = permissionState.foregroundLocationGranted,
            modifier = Modifier.fillMaxSize(),
        )


        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                when (panelState.lifecycleState) {
                    TrackingLifecycleState.Idle -> IdleTrackingView(
                        activeSport = localSport,
                        canStartTracking = canStartTracking,
                        permissionState = permissionState,
                        onSportSelected = { localSport = it },
                        onGrantLocation = {
                            foregroundLocationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                        onGrantNotifications = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onStartTracking = { viewModel.startTracking(context, localSport) },
                        errorMessage = panelState.errorMessage,
                    )
                    TrackingLifecycleState.Paused -> ActiveTrackingView(
                        panelState = panelState,
                        fallbackSport = activeSport,
                        elapsedMillis = viewModel.elapsedMillis,
                        onPause = {},
                        onResume = { viewModel.resumeTracking(context) },
                        onStop = { viewModel.stopTracking(context) },
                    )
                    TrackingLifecycleState.Running,
                    TrackingLifecycleState.Stopping,
                    TrackingLifecycleState.SaveFailed,
                    -> ActiveTrackingView(
                        panelState = panelState,
                        fallbackSport = activeSport,
                        elapsedMillis = viewModel.elapsedMillis,
                        onPause = { viewModel.pauseTracking(context) },
                        onResume = { viewModel.resumeTracking(context) },
                        onStop = { viewModel.stopTracking(context) },
                        onRetrySave = { viewModel.retrySave(context) },
                        onDiscardFailedSave = { viewModel.discardFailedSave(context) },
                    )
                }
            }
        }
    }
}

internal data class TrackingPermissionState(
    val foregroundLocationGranted: Boolean,
    val notificationPermissionRequired: Boolean,
    val notificationPermissionGranted: Boolean,
)

private fun readTrackingPermissionState(context: Context): TrackingPermissionState {
    val notificationRequired = isNotificationPermissionRequired(Build.VERSION.SDK_INT)
    return TrackingPermissionState(
        foregroundLocationGranted = context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
        notificationPermissionRequired = notificationRequired,
        notificationPermissionGranted = !notificationRequired ||
            context.hasPermission(Manifest.permission.POST_NOTIFICATIONS),
    )
}

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
