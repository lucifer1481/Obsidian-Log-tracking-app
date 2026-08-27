package com.axiel7.lucifer.ui.editmedia

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.composables.SelectableIconToggleButton
import com.axiel7.lucifer.ui.editmedia.composables.EditMediaProgressRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseEditSheet(
    sheetState: SheetState,
    initialMedia: CloudMedia,
    isAlreadySaved: Boolean = false, // 🚀 NEW: Controls Trash Icon visibility
    bottomPadding: Dp = 0.dp,
    onSave: (CloudMedia) -> Unit,
    onDelete: ((CloudMedia) -> Unit)? = null, // 🚀 NEW: Delete callback
    onDismissed: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isLoading by remember { mutableStateOf(false) }

    // 🚀 DYNAMIC MEDIA LOGIC
    val mediaTypeName = initialMedia.mediaType.uppercase()
    val isGame = mediaTypeName == MediaType.GAMES.name
    val isManga = mediaTypeName == MediaType.MANGA.name

    val defaultStatus = when {
        isGame -> "PLAN_TO_PLAY"
        isManga -> "PLAN_TO_READ"
        else -> "PLAN_TO_WATCH"
    }

    var currentScore by remember { mutableIntStateOf(initialMedia.score ?: 0) }
    var currentProgress by remember { mutableIntStateOf(initialMedia.progress ?: 0) }
    var currentStatus by remember { mutableStateOf(initialMedia.status ?: defaultStatus) }

    val statuses = remember(mediaTypeName) {
        when {
            isGame -> listOf(
                Triple("PLAN_TO_PLAY", R.drawable.ic_round_access_time_24, "Plan to Play"),
                Triple("PLAYING", R.drawable.ic_round_casino_24, "Playing"),
                Triple("COMPLETED", R.drawable.round_check_24, "Completed"),
                Triple("ON_HOLD", R.drawable.round_bookmark_24, "On Hold"),
                Triple("DROPPED", R.drawable.round_priority_high_24, "Dropped")
            )
            isManga -> listOf(
                Triple("PLAN_TO_READ", R.drawable.ic_round_access_time_24, "Plan to Read"),
                Triple("READING", R.drawable.ic_round_local_movies_24, "Reading"),
                Triple("COMPLETED", R.drawable.round_check_24, "Completed"),
                Triple("ON_HOLD", R.drawable.round_bookmark_24, "On Hold"),
                Triple("DROPPED", R.drawable.round_priority_high_24, "Dropped")
            )
            else -> listOf(
                Triple("PLAN_TO_WATCH", R.drawable.ic_round_access_time_24, "Plan to Watch"),
                Triple("WATCHING", R.drawable.ic_round_local_movies_24, "Watching"),
                Triple("COMPLETED", R.drawable.round_check_24, "Completed"),
                Triple("ON_HOLD", R.drawable.round_bookmark_24, "On Hold"),
                Triple("DROPPED", R.drawable.round_priority_high_24, "Dropped")
            )
        }
    }

    val progressLabel = when {
        isGame -> "Hours Played"
        isManga -> "Chapters Read"
        else -> "Minutes Watched"
    }

    val progressIconRes = when {
        isGame -> R.drawable.ic_round_casino_24
        else -> R.drawable.ic_round_movie_24
    }

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
            // 🚀 UPDATED HEADER: Cancel, Delete & Save Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismissed) {
                    Text(text = stringResource(R.string.cancel))
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (isAlreadySaved && onDelete != null) {
                    // 🚀 NEW: The Trash Button!
                    IconButton(
                        onClick = {
                            isLoading = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete(initialMedia)
                        }
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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

            EditMediaProgressRow(
                label = progressLabel,
                icon = progressIconRes,
                progress = currentProgress,
                modifier = Modifier.padding(start = 0.dp, end = 16.dp),
                totalProgress = null,
                onValueChange = { it.toIntOrNull()?.let { currentProgress = it } },
                minValue = 0,
                maxValue = 1000,
                onMinusClick = { if (currentProgress > 0) currentProgress-- },
                onPlusClick = { currentProgress++ }
            )

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