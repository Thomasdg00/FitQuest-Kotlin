package com.univpm.fitquest.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.components.BarChartItem
import com.univpm.fitquest.ui.components.SimpleBarChart
import com.univpm.fitquest.ui.resources.formatCaloriesMetric
import com.univpm.fitquest.ui.resources.formatDurationMinutesMetric
import com.univpm.fitquest.ui.resources.formatKilometersMetric
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.common.ScreenScaffold
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.PeriodStatsUi
import com.univpm.fitquest.viewmodel.StatsUiState
import com.univpm.fitquest.viewmodel.StatsViewModel
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenScaffold(
        title = stringResource(R.string.stats_title),
        subtitle = stringResource(R.string.stats_subtitle),
        modifier = modifier,
        onNavigateUp = onNavigateUp,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            if (!uiState.hasWorkouts) {
                EmptyStatsCard()
            }
            PeriodSummaryCard(title = stringResource(R.string.this_week), stats = uiState.weekly)
            PeriodSummaryCard(title = stringResource(R.string.this_month), stats = uiState.monthly)
            WeeklyDistanceChart(uiState = uiState)
            MonthlyTrendCard(uiState = uiState)
        }
    }
}

@Composable
private fun EmptyStatsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.stats_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun PeriodSummaryCard(
    title: String,
    stats: PeriodStatsUi,
) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = stringResource(R.string.distance),
                    value = context.formatKilometersMetric(stats.totalDistanceKm),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.stats_workout_minutes),
                    value = context.formatDurationMinutesMetric(stats.totalDurationMinutes),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = stringResource(R.string.workouts),
                    value = FormatUtils.formatWholeNumber(stats.workoutCount.toDouble()),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.calories),
                    value = context.formatCaloriesMetric(stats.totalCaloriesKcal),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeeklyDistanceChart(uiState: StatsUiState) {
    val distancePattern = stringResource(R.string.metric_distance_km)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.weekly_distance_by_sport), style = MaterialTheme.typography.titleMedium)
            SimpleBarChart(
                items = uiState.sportBreakdown.map {
                    BarChartItem(
                        label = it.sport.localizedName(),
                        value = it.weeklyDistanceKm,
                    )
                },
                valueLabel = { value ->
                    String.format(
                        Locale.ITALIAN,
                        distancePattern,
                        FormatUtils.formatOneDecimal(value),
                    )
                },
            )
        }
    }
}

@Composable
private fun MonthlyTrendCard(uiState: StatsUiState) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.monthly_trend), style = MaterialTheme.typography.titleMedium)
            SimpleBarChart(
                items = uiState.monthlyTrend.map {
                    BarChartItem(label = it.label, value = it.distanceKm)
                },
                valueLabel = { value -> context.formatKilometersMetric(value) },
            )
        }
    }
}
