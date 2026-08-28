package com.univpm.fitquest.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univpm.fitquest.R
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.domain.model.DailyForecast
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.ui.resources.weatherCodeToLabelRes
import com.univpm.fitquest.ui.resources.formatCaloriesMetric
import com.univpm.fitquest.ui.resources.formatDistanceMetric
import com.univpm.fitquest.ui.resources.formatKilometerProgressMetric
import com.univpm.fitquest.ui.resources.formatTemperatureMetric
import com.univpm.fitquest.ui.resources.formatTemperatureRangeMetric
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.history.WorkoutRouteMap
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.HomeUiState
import com.univpm.fitquest.viewmodel.HomeViewModel
import com.univpm.fitquest.domain.model.WeatherForecastResult

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onOpenStats: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentDate = remember { FormatUtils.formatCurrentDate() }

    HomeContent(
        uiState = uiState,
        currentDate = currentDate,
        modifier = modifier,
        onOpenStats = onOpenStats,
    )
}

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    currentDate: String,
    modifier: Modifier = Modifier,
    onOpenStats: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = currentDate.ifBlank { stringResource(R.string.today) },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        WeatherWidget(
            weatherState = uiState.weatherState,
            locationName = uiState.locationName,
        )

        ActivityCard(uiState = uiState)

        HomeQuickActions(onOpenStats = onOpenStats)

        LastWorkoutCard(
            workout = uiState.lastWorkout,
            routePoints = uiState.lastWorkoutRoutePoints,
        )
    }
}

@Composable
private fun HomeQuickActions(
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeQuickActionCard(
            title = stringResource(R.string.home_statistics),
            subtitle = stringResource(R.string.stats_subtitle),
            icon = Icons.Outlined.BarChart,
            onClick = onOpenStats,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WeatherWidget(
    weatherState: WeatherForecastResult,
    locationName: String?,
    modifier: Modifier = Modifier,
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
        MaterialTheme.colorScheme.surface,
    )
    val forecast = when (weatherState) {
        is WeatherForecastResult.Success -> weatherState.forecasts
        else -> emptyList()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = locationName ?: stringResource(R.string.weather),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = forecast.firstOrNull()?.let { getWeatherIcon(it.weatherCode) }
                            ?: Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }

                when (weatherState) {
                    WeatherForecastResult.Loading -> WeatherStatusMessage(R.string.weather_loading)
                    is WeatherForecastResult.Success -> {
                        weatherState.forecasts.take(3).forEachIndexed { index, day ->
                            WeatherForecastRow(
                                label = stringResource(forecastDayLabelRes(index)),
                                forecast = day,
                            )
                        }
                    }
                    WeatherForecastResult.Empty -> WeatherStatusMessage(R.string.weather_empty)
                    WeatherForecastResult.Error -> WeatherStatusMessage(R.string.weather_unavailable)
                }
            }
        }
    }
}

@Composable
private fun WeatherStatusMessage(messageRes: Int) {
    Text(
        text = stringResource(messageRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
    )
}

@Composable
private fun WeatherForecastRow(
    label: String,
    forecast: DailyForecast,
    modifier: Modifier = Modifier,
) {
    val condition = stringResource(weatherCodeToLabelRes(forecast.weatherCode))
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = condition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
        }
        Text(
            text = forecastTemperatureText(forecast),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LastWorkoutCard(
    workout: WorkoutEntity?,
    routePoints: List<RoutePointEntity>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cardShape = MaterialTheme.shapes.large
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.last_workout),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (workout == null) {
                Text(
                    text = stringResource(R.string.no_workouts_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LastWorkoutMetric(
                        label = stringResource(R.string.distance),
                        value = context.formatDistanceMetric(workout.distanceMeters),
                        modifier = Modifier.weight(1f),
                    )
                    LastWorkoutMetric(
                        label = stringResource(R.string.duration),
                        value = FormatUtils.formatDuration(workout.durationMillis),
                        modifier = Modifier.weight(1f),
                    )
                    LastWorkoutMetric(
                        label = stringResource(R.string.calories),
                        value = context.formatCaloriesMetric(workout.caloriesKcal),
                        modifier = Modifier.weight(1f),
                    )
                }
                WorkoutRouteMap(
                    routePoints = routePoints,
                    mapHeight = 150.dp,
                    showContainer = false,
                    shape = cardShape,
                )
            }
        }
    }
}

@Composable
private fun LastWorkoutMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivityCard(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.weekly_goal_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            WeeklySportProgressCards(
                weeklyDistances = uiState.weeklyDistances,
                weeklyGoals = uiState.weeklyGoals,
            )
        }
    }
}

@Composable
private fun WeeklySportProgressCards(
    weeklyDistances: Map<Sport, Double>,
    weeklyGoals: Map<Sport, Double>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Sport.entries.forEach { sport ->
            val distance = weeklyDistances[sport] ?: 0.0
            val goal = weeklyGoals[sport] ?: 1.0
            val progressFraction = if (goal <= 0.0) 0f else (distance / goal).toFloat().coerceIn(0f, 1f)

            SportProgressCard(
                sport = sport,
                distance = distance,
                goal = goal,
                progressFraction = progressFraction,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SportProgressCard(
    sport: Sport,
    distance: Double,
    goal: Double,
    progressFraction: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sportColor = when (sport) {
        Sport.Walking -> MaterialTheme.colorScheme.primary
        Sport.Running -> MaterialTheme.colorScheme.secondary
        Sport.Cycling -> MaterialTheme.colorScheme.tertiary
    }
    val sportName = sport.localizedName()

    val sportIcon = when (sport) {
        Sport.Walking -> Icons.AutoMirrored.Outlined.DirectionsWalk
        Sport.Running -> Icons.AutoMirrored.Outlined.DirectionsRun
        Sport.Cycling -> Icons.AutoMirrored.Outlined.DirectionsBike
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = sportIcon,
                contentDescription = null,
                tint = sportColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = sportName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = sportColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                strokeCap = StrokeCap.Round,
            )

            Text(
                text = context.formatKilometerProgressMetric(distance, goal),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun getWeatherIcon(weatherCode: Int?): ImageVector = when (weatherCode) {
    null -> Icons.Outlined.CloudOff
    0 -> Icons.Outlined.WbSunny
    1, 2, 3 -> Icons.Outlined.Cloud
    45, 48 -> Icons.Outlined.Cloud
    51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Outlined.Umbrella
    71, 73, 75, 77, 85, 86 -> Icons.Outlined.AcUnit
    95, 96, 99 -> Icons.Outlined.Thunderstorm
    else -> Icons.Outlined.Cloud
}

private fun forecastDayLabelRes(index: Int): Int = when (index) {
    0 -> R.string.today
    1 -> R.string.tomorrow
    else -> R.string.day_after_tomorrow
}

@Composable
private fun forecastTemperatureText(forecast: DailyForecast): String {
    val context = LocalContext.current
    val max = forecast.maxTemperatureCelsius
    val min = forecast.minTemperatureCelsius
    return when {
        max != null && min != null -> context.formatTemperatureRangeMetric(min, max)
        max != null -> context.formatTemperatureMetric(max)
        min != null -> context.formatTemperatureMetric(min)
        else -> "--"
    }
}
