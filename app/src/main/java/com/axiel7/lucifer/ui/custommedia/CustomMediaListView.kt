package com.axiel7.lucifer.ui.custommedia

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.navigation.NavActionManager
import com.axiel7.lucifer.ui.editmedia.SupabaseEditSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMediaListView(
    mediaType: String,
    title: String,
    padding: PaddingValues = PaddingValues(),
    navActionManager: NavActionManager, // 🚀 NEW: Added to allow navigation to Details
    viewModel: CustomMediaViewModel = koinViewModel()
) {
    val uiState by uiStateFlow(viewModel, mediaType)

    // STATE FOR EDIT SHEET
    var selectedMedia by remember { mutableStateOf<CloudMedia?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val filterOptions = remember(mediaType) {
        when (mediaType.uppercase()) {
            "GAMES" -> listOf("ALL", "PLAYING", "COMPLETED", "PLAN_TO_PLAY", "ON_HOLD", "DROPPED")
            "MANGA" -> listOf("ALL", "READING", "COMPLETED", "PLAN_TO_READ", "ON_HOLD", "DROPPED")
            else -> listOf("ALL", "WATCHING", "COMPLETED", "PLAN_TO_WATCH", "ON_HOLD", "DROPPED")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = padding.calculateBottomPadding())
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(
                start = 16.dp,
                top = padding.calculateTopPadding() + 24.dp,
                bottom = 8.dp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { status ->
                FilterChip(
                    selected = uiState.selectedStatus == status,
                    onClick = { viewModel.onStatusFilterChanged(status) },
                    label = {
                        Text(
                            text = status.split("_").joinToString(" ") { word ->
                                word.lowercase().replaceFirstChar { it.uppercase() }
                            }
                        )
                    }
                )
            }
        }

        val filteredList = if (uiState.selectedStatus == "ALL") {
            uiState.mediaList
        } else {
            uiState.mediaList.filter { it.status.equals(uiState.selectedStatus, ignoreCase = true) }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No items in your $title list yet!\nAdd items from the Search or Explore tabs.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id ?: it.apiId }) { media ->
                    CustomMediaItemCard(
                        media = media,
                        onClick = {
                            // 🚀 SINGLE TAP: Navigate to Details screen
                            val typeEnum = MediaType.valueOf(mediaType.uppercase())
                            navActionManager.toMediaDetails(typeEnum, media.apiId.toInt())
                        },
                        onLongClick = {
                            // 🚀 LONG PRESS: Open Edit/Delete Sheet
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedMedia = media
                        }
                    )
                }
            }
        }
    }

    if (selectedMedia != null) {
        SupabaseEditSheet(
            sheetState = sheetState,
            initialMedia = selectedMedia!!,
            isAlreadySaved = true,
            onSave = { updatedMedia ->
                // Ensure viewModel.updateMedia() exists in CustomMediaViewModel
                // viewModel.updateMedia(updatedMedia)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    selectedMedia = null
                }
            },
            onDelete = { mediaToDelete ->
                // Ensure viewModel.deleteMedia() exists in CustomMediaViewModel
                // viewModel.deleteMedia(mediaToDelete)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    selectedMedia = null
                }
            },
            onDismissed = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    selectedMedia = null
                }
            }
        )
    }
}

@Composable
private fun uiStateFlow(viewModel: CustomMediaViewModel, mediaType: String) =
    viewModel.uiState.collectAsStateWithLifecycle().also {
        LaunchedEffect(mediaType) {
            viewModel.loadMedia(mediaType)
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomMediaItemCard(
    media: CloudMedia,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            // 🚀 USE COMBINED CLICKABLE for Single Tap and Long Press
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = media.imageUrl,
                    contentDescription = media.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if ((media.score ?: 0) > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_round_star_16),
                                contentDescription = "score",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${media.score}",
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = media.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}