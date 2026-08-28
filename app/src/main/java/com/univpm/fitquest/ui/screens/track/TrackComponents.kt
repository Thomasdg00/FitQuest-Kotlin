package com.univpm.fitquest.ui.screens.track

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.service.TrackingLifecycleState
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.resources.formatCaloriesMetric
import com.univpm.fitquest.ui.resources.formatDistanceMetric
import com.univpm.fitquest.ui.resources.formatElevationMetric
import com.univpm.fitquest.ui.resources.formatPaceMetric
import com.univpm.fitquest.ui.resources.formatSpeedMetric
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.TrackPanelUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun IdleTrackingView(
    activeSport: Sport,
    canStartTracking: Boolean,
    permissionState: TrackingPermissionState,
    onSportSelected: (Sport) -> Unit,
    onGrantLocation: () -> Unit,
    onGrantNotifications: () -> Unit,
    onStartTracking: () -> Unit,
    errorMessage: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SportSelector(
            activeSport = activeSport,
            onSportSelected = onSportSelected,
        )

        if (canStartTracking) {
            Button(
                onClick = onStartTracking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeSport.accentColor(),
                    contentColor = activeSport.accentContentColor(),
                ),
            ) {
                Text(
                    text = stringResource(R.string.track_start_activity),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        } else {
            PermissionRequiredPrompt(
                permissionState = permissionState,
                onGrantLocation = onGrantLocation,
                onGrantNotifications = onGrantNotifications,
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun ActiveTrackingView(
    panelState: TrackPanelUiState,
    fallbackSport: Sport,
    elapsedMillis: StateFlow<Long>,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onRetrySave: () -> Unit = {},
    onDiscardFailedSave: () -> Unit = {},
) {
    val currentSport = panelState.sport ?: fallbackSport
    val sportAccentColor = currentSport.accentColor()
    val sportAccentContentColor = currentSport.accentContentColor()
    var showDiscardDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = currentSport.icon(),
                    contentDescription = null,
                    tint = sportAccentColor,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = currentSport.localizedName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            TimerText(
                elapsedMillis = elapsedMillis,
                color = sportAccentColor,
            )
        }

        TrackingStatsPanel(
            panelState = panelState,
            currentSport = currentSport,
        )

        panelState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        TrackingControls(
            lifecycleState = panelState.lifecycleState,
            accentColor = sportAccentColor,
            accentContentColor = sportAccentContentColor,
            onPause = onPause,
            onResume = onResume,
            onStop = onStop,
            onRetrySave = onRetrySave,
            onDiscardFailedSave = { showDiscardDialog = true },
        )
    }

    if (showDiscardDialog && panelState.lifecycleState == TrackingLifecycleState.SaveFailed) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.track_discard_confirm_title)) },
            text = { Text(stringResource(R.string.track_discard_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onDiscardFailedSave()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.track_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
internal fun TimerText(
    elapsedMillis: StateFlow<Long>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val value by elapsedMillis.collectAsState()
    Text(
        text = FormatUtils.formatDuration(value),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier,
    )
}

@Composable
internal fun SportSelector(
    activeSport: Sport,
    onSportSelected: (Sport) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Sport.entries.forEach { sport ->
            val sportName = sport.localizedName()
            val isSelected = activeSport == sport
            val accentColor = sport.accentColor()
            Surface(
                modifier = Modifier.weight(1f),
                shape = ButtonDefaults.outlinedShape,
                color = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { onSportSelected(sport) },
                            role = Role.RadioButton,
                        )
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = sport.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = sportName,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
internal fun TrackingStatsPanel(
    panelState: TrackPanelUiState,
    currentSport: Sport,
) {
    val context = LocalContext.current
    val speedMps = panelState.currentSpeedMetersPerSecond
    val isCycling = currentSport == Sport.Cycling
    val speedOrPaceLabel = if (isCycling) stringResource(R.string.speed) else stringResource(R.string.pace)
    val speedOrPaceValue = if (isCycling) {
        speedMps?.let(context::formatSpeedMetric) ?: "--"
    } else {
        context.formatPaceMetric(speedMps)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricItem(
                label = stringResource(R.string.distance),
                value = context.formatDistanceMetric(panelState.distanceMeters),
                modifier = Modifier.weight(1f),
            )
            MetricItem(
                label = speedOrPaceLabel,
                value = speedOrPaceValue,
                modifier = Modifier.weight(1f),
            )
            MetricItem(
                label = stringResource(R.string.calories),
                value = context.formatCaloriesMetric(panelState.estimatedCaloriesKcal),
                modifier = Modifier.weight(1f),
            )
        }

        MetricItem(
            label = stringResource(R.string.live_elevation),
            value = panelState.currentAltitudeMeters?.let(context::formatElevationMetric)
                ?: stringResource(R.string.elevation_unavailable),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun TrackingControls(
    lifecycleState: TrackingLifecycleState,
    accentColor: Color,
    accentContentColor: Color,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onRetrySave: () -> Unit,
    onDiscardFailedSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (lifecycleState) {
            TrackingLifecycleState.Running -> {
                val pauseText = stringResource(R.string.pause)
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = accentContentColor,
                    ),
                ) {
                    Icon(imageVector = Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(pauseText)
                }
            }
            TrackingLifecycleState.Paused -> {
                val resumeText = stringResource(R.string.resume)
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = accentContentColor,
                    ),
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(resumeText)
                }
            }
            TrackingLifecycleState.Stopping -> {
                Button(
                    enabled = false,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.track_saving_activity))
                }
            }
            TrackingLifecycleState.SaveFailed -> {
                val retryText = stringResource(R.string.track_retry_save)
                Button(
                    onClick = onRetrySave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = accentContentColor,
                    ),
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(retryText)
                }
                val discardText = stringResource(R.string.track_discard_workout)
                OutlinedButton(
                    onClick = onDiscardFailedSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(discardText)
                }
            }
            TrackingLifecycleState.Idle -> Unit
        }

        if (
            lifecycleState == TrackingLifecycleState.Running ||
            lifecycleState == TrackingLifecycleState.Paused
        ) {
            val stopText = stringResource(R.string.stop)
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stopText)
            }
        }
    }
}


@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Sport.accentColor(): Color {
    return when (this) {
        Sport.Walking -> MaterialTheme.colorScheme.primary
        Sport.Running -> MaterialTheme.colorScheme.secondary
        Sport.Cycling -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun Sport.accentContentColor(): Color {
    return when (this) {
        Sport.Walking -> MaterialTheme.colorScheme.onPrimary
        Sport.Running -> MaterialTheme.colorScheme.onSecondary
        Sport.Cycling -> MaterialTheme.colorScheme.onTertiary
    }
}

private fun Sport.icon(): ImageVector {
    return when (this) {
        Sport.Walking -> Icons.AutoMirrored.Outlined.DirectionsWalk
        Sport.Running -> Icons.AutoMirrored.Outlined.DirectionsRun
        Sport.Cycling -> Icons.AutoMirrored.Outlined.DirectionsBike
    }
}
