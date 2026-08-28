package com.univpm.fitquest.ui.screens.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R

@Composable
internal fun PermissionRequiredPrompt(
    permissionState: TrackingPermissionState,
    onGrantLocation: () -> Unit,
    onGrantNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.track_permissions_required),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Text(
            text = stringResource(R.string.track_permissions_body),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!permissionState.foregroundLocationGranted) {
                Button(
                    onClick = onGrantLocation,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.track_grant_location),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (permissionState.notificationPermissionRequired && !permissionState.notificationPermissionGranted) {
                Button(
                    onClick = onGrantNotifications,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.track_grant_notifications),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
