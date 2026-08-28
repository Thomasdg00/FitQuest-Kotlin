package com.univpm.fitquest.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
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
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.common.ScreenScaffold
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.HistoryUiState
import com.univpm.fitquest.viewmodel.HistoryViewModel
import com.univpm.fitquest.viewmodel.metersToKm
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenWorkout: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryContent(
        uiState = uiState,
        onOpenWorkout = onOpenWorkout,
        modifier = modifier,
    )
}

@Composable
internal fun HistoryContent(
    uiState: HistoryUiState,
    onOpenWorkout: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = stringResource(R.string.history_title),
        subtitle = stringResource(R.string.history_subtitle),
        modifier = modifier,
    ) {
        if (uiState.workouts.isEmpty()) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.workouts, key = { it.id }) { workout ->
                    WorkoutHistoryCard(
                        workout = workout,
                        onClick = { onOpenWorkout(workout.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    workout: WorkoutEntity,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val sport = Sport.fromRouteValue(workout.sport)
    val sportName = sport.localizedName()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = sportName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT,
                    Locale.ITALIAN,
                )
                    .format(Date(workout.startedAtMillis)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    R.string.history_workout_summary,
                    FormatUtils.formatOneDecimal(metersToKm(workout.distanceMeters)),
                    FormatUtils.formatDurationMinutes(workout.durationMillis),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
