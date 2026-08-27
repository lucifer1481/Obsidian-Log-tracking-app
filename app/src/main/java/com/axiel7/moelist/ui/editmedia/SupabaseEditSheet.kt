package com.axiel7.moelist.ui.editmedia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.custom.CloudMedia
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.SelectableIconToggleButton
import com.axiel7.moelist.ui.editmedia.composables.EditMediaProgressRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseEditSheet(
    sheetState: SheetState,
    initialMedia: CloudMedia,
    bottomPadding: Dp = 0.dp,
    onSave: (CloudMedia) -> Unit,
    onDismissed: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isLoading by remember { mutableStateOf(false) }

    // 🚀 Local State for Editing
    var currentScore by remember { mutableIntStateOf(initialMedia.score ?: 0) }
    var currentProgress by remember { mutableIntStateOf(initialMedia.progress ?: 0) }
    var currentStatus by remember { mutableStateOf(initialMedia.status ?: "PLAN_TO_WATCH") }
    // Status Options aligned with your aesthetic
    // Status Options aligned with verified icons from your project
    val statuses = listOf(
        Triple("PLAN_TO_WATCH", R.drawable.ic_round_access_time_24, "Plan to Watch / Play"),
        Triple("WATCHING", R.drawable.ic_round_local_movies_24, "Currently Watching / Playing"),
        Triple("COMPLETED", R.drawable.round_check_24, "Completed"),
        Triple("ON_HOLD", R.drawable.round_bookmark_24, "On Hold"),
        Triple("DROPPED", R.drawable.round_priority_high_24, "Dropped")
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissed,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp + bottomPadding)
                .imePadding(),
        ) {
            // Header: Cancel & Save Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismissed) {
                    Text(text = stringResource(R.string.cancel))
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }

                Button(onClick = {
                    isLoading = true
                    onSave(
                        initialMedia.copy(
                            score = currentScore,
                            progress = currentProgress,
                            status = currentStatus
                        )
                    )
                }) {
                    Text(text = "Save to Library")
                }
            }

            // Status Selector (The Green Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                statuses.forEach { (statusId, iconRes, label) ->
                    SelectableIconToggleButton(
                        icon = iconRes,
                        tooltipText = label,
                        value = statusId,
                        selectedValue = currentStatus,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentStatus = statusId
                        }
                    )
                }
            }

            // Progress Tracker
            EditMediaProgressRow(
                label = if (initialMedia.mediaType == MediaType.GAMES.name) "Hours Played" else "Minutes Watched",
                icon = if (initialMedia.mediaType == MediaType.GAMES.name) R.drawable.ic_round_casino_24 else R.drawable.ic_round_movie_24,
                progress = currentProgress,
                modifier = Modifier.padding(start = 0.dp, end = 16.dp),
                totalProgress = null, // External media doesn't have a strict max progress locally
                onValueChange = { it.toIntOrNull()?.let { currentProgress = it } },
                minValue = 0,
                maxValue = 1000,
                onMinusClick = { if (currentProgress > 0) currentProgress-- },
                onPlusClick = { currentProgress++ }
            )

            // Score Tracker (Out of 10)
            EditMediaProgressRow(
                label = "$currentScore / 10",
                icon = R.drawable.ic_round_details_star_24,
                progress = currentScore,
                modifier = Modifier.padding(start = 0.dp, top = 8.dp, end = 16.dp),
                totalProgress = 10,
                onValueChange = { value -> value.toIntOrNull()?.let { currentScore = it } },
                minValue = 0,
                maxValue = 10,
                onMinusClick = { if (currentScore > 0) currentScore-- },
                onPlusClick = { if (currentScore < 10) currentScore++ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }
    }
}