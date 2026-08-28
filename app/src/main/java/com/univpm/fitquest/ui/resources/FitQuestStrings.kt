package com.univpm.fitquest.ui.resources

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.Sport

@StringRes
fun Sport.nameRes(): Int = when (this) {
    Sport.Walking -> R.string.sport_walking
    Sport.Running -> R.string.sport_running
    Sport.Cycling -> R.string.sport_cycling
}

@Composable
fun Sport.localizedName(): String = stringResource(nameRes())

fun Context.getSportName(sport: Sport): String = getString(sport.nameRes())
