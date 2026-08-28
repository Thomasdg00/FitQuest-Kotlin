package com.univpm.fitquest.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.ui.components.formatChartMeters
import com.univpm.fitquest.ui.components.formatChartPace
import com.univpm.fitquest.ui.components.LineChartPoint
import com.univpm.fitquest.ui.components.SimpleLineChart
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.resources.formatCaloriesMetric
import com.univpm.fitquest.ui.resources.formatDistanceMetric
import com.univpm.fitquest.ui.resources.formatSpeedMetric
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.WorkoutDetailEvent
import com.univpm.fitquest.viewmodel.WorkoutDetailViewModel
import com.univpm.fitquest.viewmodel.ChartPointUi
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    viewModel: WorkoutDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val durationPattern = stringResource(R.string.metric_duration_minutes)
    val pacePattern = stringResource(R.string.metric_pace_min_km)
    val distancePattern = stringResource(R.string.metric_distance_km)
    val elevationPattern = stringResource(R.string.metric_elevation_m)
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteFailedMessage = stringResource(R.string.workout_delete_failed)
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                WorkoutDetailEvent.WorkoutDeleted -> onDeleted()
                WorkoutDetailEvent.DeleteFailed -> snackbarHostState.showSnackbar(deleteFailedMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.workout_detail_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val workout = uiState.workout
            if (workout == null) {
                EmptyDetailCard()
            } else {
                WorkoutSummaryCard(workout = workout)
                WorkoutRouteMap(routePoints = uiState.routePoints)
                WorkoutLineChartCard(
                    title = stringResource(R.string.pace_over_time),
                    points = uiState.routeCharts.pacePoints,
                    emptyText = stringResource(R.string.pace_chart_unavailable),
                    xAxisLabel = stringResource(R.string.chart_axis_time_minutes),
                    yAxisLabel = stringResource(R.string.chart_axis_pace),
                    xValueLabel = { value ->
                        String.format(
                            Locale.ITALIAN,
                            durationPattern,
                            FormatUtils.formatOneDecimal(value),
                        )
                    },
                    yValueLabel = { value ->
                        String.format(
                            Locale.ITALIAN,
                            pacePattern,
                            formatChartPace(value),
                        )
                    },
                )
                WorkoutLineChartCard(
                    title = stringResource(R.string.elevation_profile),
                    points = uiState.routeCharts.elevationPoints,
                    emptyText = stringResource(R.string.elevation_chart_unavailable),
                    xAxisLabel = stringResource(R.string.chart_axis_distance),
                    yAxisLabel = stringResource(R.string.chart_axis_elevation),
                    xValueLabel = { value ->
                        String.format(
                            Locale.ITALIAN,
                            distancePattern,
                            FormatUtils.formatOneDecimal(value),
                        )
                    },
                    yValueLabel = { value ->
                        String.format(
                            Locale.ITALIAN,
                            elevationPattern,
                            formatChartMeters(value),
                        )
                    },
                    lineColor = MaterialTheme.colorScheme.tertiary,
                )
                DeleteWorkoutButton(
                    isDeleting = uiState.isDeleting,
                    onClick = { showDeleteDialog = true },
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteWorkoutDialog(
            isDeleting = uiState.isDeleting,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteWorkout()
            },
        )
    }
}

@Composable
private fun EmptyDetailCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.workout_not_found),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun WorkoutSummaryCard(workout: WorkoutEntity) {
    val context = LocalContext.current
    val sportName = Sport.fromRouteValue(workout.sport).localizedName()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = sportName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = DateFormat.getDateTimeInstance(
                    DateFormat.DEFAULT,
                    DateFormat.DEFAULT,
                    Locale.ITALIAN,
                ).format(Date(workout.startedAtMillis)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailStat(stringResource(R.string.distance), context.formatDistanceMetric(workout.distanceMeters), Modifier.weight(1f))
                DetailStat(stringResource(R.string.duration), FormatUtils.formatDuration(workout.durationMillis), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailStat(stringResource(R.string.calories), context.formatCaloriesMetric(workout.caloriesKcal), Modifier.weight(1f))
                DetailStat(stringResource(R.string.avg_speed), context.formatSpeedMetric(workout.averageSpeedMetersPerSecond), Modifier.weight(1f))
            }
            Text(
                text = stringResource(
                    R.string.detail_elevation,
                    FormatUtils.formatWholeNumber(workout.elevationGainMeters),
                    FormatUtils.formatWholeNumber(workout.elevationLossMeters),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DetailStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun WorkoutLineChartCard(
    title: String,
    points: List<ChartPointUi>,
    emptyText: String,
    xAxisLabel: String,
    yAxisLabel: String,
    xValueLabel: (Double) -> String,
    yValueLabel: (Double) -> String,
    lineColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    val semanticDescription = if (points.size >= 2) {
        stringResource(
            R.string.chart_semantics_summary,
            title,
            points.size,
            yValueLabel(points.minOf { it.y }),
            yValueLabel(points.maxOf { it.y }),
        )
    } else {
        emptyText
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            SimpleLineChart(
                points = points.map { LineChartPoint(it.x, it.y) },
                emptyText = emptyText,
                semanticDescription = semanticDescription,
                lineColor = lineColor,
                xAxisLabel = xAxisLabel,
                yAxisLabel = yAxisLabel,
                xValueLabel = xValueLabel,
                yValueLabel = yValueLabel,
            )
        }
    }
}

@Composable
private fun DeleteWorkoutButton(
    isDeleting: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isDeleting,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
        )
        Text(
            text = if (isDeleting) {
                stringResource(R.string.workout_delete_deleting)
            } else {
                stringResource(R.string.workout_delete)
            },
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun DeleteWorkoutDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        title = { Text(stringResource(R.string.workout_delete_confirm_title)) },
        text = { Text(stringResource(R.string.workout_delete_confirm_body)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.workout_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting,
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
