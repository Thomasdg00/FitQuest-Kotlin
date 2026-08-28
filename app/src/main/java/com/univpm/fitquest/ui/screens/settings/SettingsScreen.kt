package com.univpm.fitquest.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.ThemeMode
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.common.ScreenScaffold
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.SettingsUiState
import com.univpm.fitquest.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        modifier = modifier,
        onSaveBodyWeight = viewModel::saveBodyWeight,
        onThemeSelected = viewModel::saveThemeMode,
        onSaveWeeklyGoals = viewModel::saveWeeklyGoals,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    onSaveBodyWeight: (Double) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onSaveWeeklyGoals: (Map<Sport, Double>) -> Unit,
) {
    val weightInput = rememberSaveable(uiState.bodyWeightKg, saver = TextFieldState.Saver) {
        TextFieldState(formatBodyWeightInput(uiState.bodyWeightKg))
    }
    var weightError by remember { mutableStateOf<SettingsInputError?>(null) }
    val weeklyGoalInputs = rememberWeeklyGoalInputs()
    val weeklyGoalErrors = remember { androidx.compose.runtime.mutableStateMapOf<String, SettingsInputError>() }

    LaunchedEffect(weightInput.text) { weightError = null }
    LaunchedEffect(uiState.weeklyGoalKmBySport) {
        Sport.entries.forEach { sport ->
            weeklyGoalInputs[sport.routeValue] = FormatUtils.formatOneDecimal(
                uiState.weeklyGoalKmBySport[sport] ?: 0.0,
            )
            weeklyGoalErrors.remove(sport.routeValue)
        }
    }

    ScreenScaffold(
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            BodyWeightCard(
                weightInput = weightInput,
                weightError = weightError,
                onSave = {
                    val result = parseBodyWeightKgInput(weightInput.text.toString())
                    weightError = result.error
                    result.value?.let(onSaveBodyWeight)
                },
            )
            AppearanceCard(
                selectedTheme = uiState.themeMode,
                onThemeSelected = onThemeSelected,
            )
            WeeklyGoalsCard(
                goalInputs = weeklyGoalInputs,
                goalErrors = weeklyGoalErrors,
                onGoalInputChange = { sport, value ->
                    weeklyGoalInputs[sport.routeValue] = value
                    weeklyGoalErrors.remove(sport.routeValue)
                },
                onSaveGoals = {
                    val parsedGoals = mutableMapOf<Sport, Double>()
                    weeklyGoalErrors.clear()
                    Sport.entries.forEach { sport ->
                        val result = parseWeeklyGoalKmInput(
                            input = weeklyGoalInputs[sport.routeValue].orEmpty(),
                            currentValue = uiState.weeklyGoalKmBySport[sport] ?: 0.0,
                        )
                        if (result.error != null) {
                            weeklyGoalErrors[sport.routeValue] = result.error
                        } else {
                            parsedGoals[sport] = checkNotNull(result.value)
                        }
                    }
                    if (weeklyGoalErrors.isEmpty()) onSaveWeeklyGoals(parsedGoals)
                },
            )
        }
    }
}

internal fun formatBodyWeightInput(value: Double): String = FormatUtils.formatOneDecimal(value)

@Composable
private fun rememberWeeklyGoalInputs(): SnapshotStateMap<String, String> =
    remember { androidx.compose.runtime.mutableStateMapOf() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BodyWeightCard(
    weightInput: TextFieldState,
    weightError: SettingsInputError?,
    onSave: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(stringResource(R.string.body_weight), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.body_weight_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                state = weightInput,
                labelPosition = TextFieldLabelPosition.Attached(alwaysMinimize = true),
                label = { Text(stringResource(R.string.weight)) },
                suffix = { Text(stringResource(R.string.unit_kg)) },
                isError = weightError != null,
                supportingText = weightError?.let { error ->
                    { Text(stringResource(error.messageResId())) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = onSave) { Text(stringResource(R.string.save)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceCard(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = stringResource(selectedTheme.labelResId()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.theme)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    ThemeMode.entries.forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(stringResource(theme.labelResId())) },
                            onClick = {
                                onThemeSelected(theme)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyGoalsCard(
    goalInputs: Map<String, String>,
    goalErrors: Map<String, SettingsInputError>,
    onGoalInputChange: (Sport, String) -> Unit,
    onSaveGoals: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(stringResource(R.string.weekly_goals), style = MaterialTheme.typography.titleMedium)
            Sport.entries.forEach { sport ->
                val error = goalErrors[sport.routeValue]
                OutlinedTextField(
                    value = goalInputs[sport.routeValue].orEmpty(),
                    onValueChange = { onGoalInputChange(sport, it) },
                    label = { Text(sport.localizedName()) },
                    isError = error != null,
                    supportingText = {
                        Text(stringResource(error?.messageResId() ?: R.string.goal_km_per_week))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(onClick = onSaveGoals) { Text(stringResource(R.string.save)) }
        }
    }
}

private fun ThemeMode.labelResId(): Int = when (this) {
    ThemeMode.Light -> R.string.theme_light
    ThemeMode.Dark -> R.string.theme_dark
}

internal enum class SettingsInputError {
    Required,
    WeightKg,
    WeeklyGoalKm,
}

internal data class SettingsInputResult<T>(
    val value: T? = null,
    val error: SettingsInputError? = null,
)

internal fun parseBodyWeightKgInput(input: String): SettingsInputResult<Double> {
    if (input.isBlank()) return SettingsInputResult(error = SettingsInputError.Required)
    val value = input.toFiniteDecimalOrNull()
    return if (value != null && value in 20.0..300.0) {
        SettingsInputResult(value)
    } else {
        SettingsInputResult(error = SettingsInputError.WeightKg)
    }
}

internal fun parseWeeklyGoalKmInput(
    input: String,
    currentValue: Double,
): SettingsInputResult<Double> {
    if (input.isBlank()) return SettingsInputResult(currentValue)
    val value = input.toFiniteDecimalOrNull()
    return if (value != null && value >= 0.0) {
        SettingsInputResult(value)
    } else {
        SettingsInputResult(error = SettingsInputError.WeeklyGoalKm)
    }
}

private fun String.toFiniteDecimalOrNull(): Double? {
    val value = trim().replace(',', '.').toDoubleOrNull() ?: return null
    return value.takeIf { it.isFinite() }
}

private fun SettingsInputError.messageResId(): Int = when (this) {
    SettingsInputError.Required -> R.string.settings_error_required
    SettingsInputError.WeightKg -> R.string.settings_error_weight_kg
    SettingsInputError.WeeklyGoalKm -> R.string.settings_error_goal_km
}
