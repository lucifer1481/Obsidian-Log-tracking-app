package com.axiel7.lucifer.ui.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.anime.AnimeDetails
import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.model.manga.MangaDetails
import com.axiel7.lucifer.data.model.media.MediaStatus
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.navigation.NavActionManager
import com.axiel7.lucifer.ui.composables.InfoTitle
import com.axiel7.lucifer.ui.composables.TextIconHorizontal
import com.axiel7.lucifer.ui.composables.TextIconVertical
import com.axiel7.lucifer.ui.composables.defaultPlaceholder
import com.axiel7.lucifer.ui.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.lucifer.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.lucifer.ui.composables.media.MediaItemVertical
import com.axiel7.lucifer.ui.composables.media.MediaPoster
import com.axiel7.lucifer.ui.composables.stats.HorizontalStatsBar
import com.axiel7.lucifer.ui.details.composables.AnimeThemeItem
import com.axiel7.lucifer.ui.details.composables.MediaDetailsTopAppBar
import com.axiel7.lucifer.ui.details.composables.MediaInfoView
import com.axiel7.lucifer.ui.details.composables.MusicStreamingSheet
import com.axiel7.lucifer.ui.editmedia.EditMediaSheet
import com.axiel7.lucifer.ui.editmedia.SupabaseEditSheet
import com.axiel7.lucifer.ui.theme.MoeListTheme
import com.axiel7.lucifer.utils.CHARACTER_URL
import com.axiel7.lucifer.utils.ContextExtensions.copyToClipBoard
import com.axiel7.lucifer.utils.ContextExtensions.getCurrentLanguageTag
import com.axiel7.lucifer.utils.ContextExtensions.openLink
import com.axiel7.lucifer.utils.ContextExtensions.showToast
import com.axiel7.lucifer.utils.DateUtils.parseDateAndLocalize
import com.axiel7.lucifer.utils.NumExtensions.format
import com.axiel7.lucifer.utils.StringExtensions.toStringOrNull
import com.axiel7.lucifer.utils.TranslateUtils.openTranslator
import com.axiel7.lucifer.utils.UNKNOWN_CHAR
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaDetailsView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager
) {
    val viewModel: MediaDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MediaDetailsContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MediaDetailsContent(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    var showExternalSheet by remember { mutableStateOf(false) }

    fun hideSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            showSheet = false
            showExternalSheet = false
        }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var isSynopsisExpanded by remember { mutableStateOf(false) }
    val maxLinesSynopsis by remember {
        derivedStateOf { if (isSynopsisExpanded) Int.MAX_VALUE else 5 }
    }
    val iconExpand by remember {
        derivedStateOf {
            if (isSynopsisExpanded) R.drawable.ic_round_keyboard_arrow_up_24
            else R.drawable.ic_round_keyboard_arrow_down_24
        }
    }
    val isCurrentLanguageEn = remember { getCurrentLanguageTag()?.startsWith("en") }

    if (showSheet && uiState.mediaInfo != null) {
        EditMediaSheet(
            sheetState = sheetState,
            mediaInfo = uiState.mediaInfo!!,
            myListStatus = uiState.myListStatus,
            bottomPadding = bottomBarPadding,
            onEdited = { status, removed ->
                hideSheet()
                event?.onChangedMyListStatus(status, removed)
            },
            onDismissed = { hideSheet() }
        )
    }

    if (showExternalSheet) {
        // 🚀 FIXED: Grab the actual ViewModel instance so we can pre-populate the sheet correctly
        val vm = event as? MediaDetailsViewModel

        // Use the saved entry from Supabase if it exists; otherwise build a new one
        val initialMedia = vm?.savedCloudMedia ?: CloudMedia(
            apiId = vm?.currentMediaId?.toLong() ?: 0,
            mediaType = uiState.mediaType.name,
            title = uiState.externalTitle ?: "Unknown",
            imageUrl = uiState.externalPoster,
            score = uiState.externalScore?.toInt() ?: 0,
            progress = 0,
            status = "PLAN_TO_WATCH"
        )

        SupabaseEditSheet(
            sheetState = sheetState,
            initialMedia = initialMedia,
            isAlreadySaved = uiState.isSavedInSupabase, // 🚀 NEW: Enables the trash button
            bottomPadding = bottomBarPadding,
            onSave = { updatedMedia ->
                vm?.saveExternalMedia(updatedMedia)
                hideSheet()
            },
            onDelete = { mediaToDelete ->               // 🚀 NEW: Triggers the delete function
                vm?.deleteExternalMedia(mediaToDelete)
                hideSheet()
            },
            onDismissed = { hideSheet() }
        )
    }

    if (uiState.message != null) {
        LaunchedEffect(uiState.message) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediaDetailsTopAppBar(
                uiState = uiState,
                event = event,
                navigateBack = dropUnlessResumed { navActionManager.goBack() },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if (uiState.isExternal) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // 🚀 FIXED: Button now ALWAYS opens the edit sheet!
                        showExternalSheet = true
                    }
                ) {
                    Icon(
                        painter = painterResource(if (uiState.isSavedInSupabase) R.drawable.ic_round_edit_24 else R.drawable.ic_round_add_24),
                        contentDescription = "edit"
                    )
                    Text(
                        // 🚀 FIXED: Updates text dynamically to "Edit Entry" if you've saved it already
                        text = if (uiState.isSavedInSupabase) "Edit Entry" else "Add to Library",
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    )
                }
            } else if (isLoggedIn) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (uiState.mediaDetails != null) {
                            showSheet = true
                        } else {
                            context.showToast(context.getString(R.string.please_login_to_use_this_feature))
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if (uiState.isNewEntry) R.drawable.ic_round_add_24
                            else R.drawable.ic_round_edit_24
                        ),
                        contentDescription = "edit"
                    )
                    Text(
                        text = if (uiState.isNewEntry) stringResource(R.string.add)
                        else uiState.mediaDetails?.myListStatus?.status?.localized()
                            ?: stringResource(R.string.edit),
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(bottom = 88.dp)
        ) {
            Row {
                val posterUrl = if (uiState.isExternal) uiState.externalPoster else uiState.mediaDetails?.mainPicture?.large
                MediaPoster(
                    url = posterUrl,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .size(
                            width = MEDIA_POSTER_BIG_WIDTH.dp,
                            height = MEDIA_POSTER_BIG_HEIGHT.dp
                        )
                        .defaultPlaceholder(visible = uiState.isLoading)
                        .clickable(onClick = dropUnlessResumed {
                            if (uiState.picturesUrls.isNotEmpty())
                                navActionManager.toFullPoster(uiState.picturesUrls)
                        })
                )
                Column {
                    val title = if (uiState.isExternal) uiState.externalTitle ?: "Loading" else uiState.mediaDetails?.userPreferredTitle() ?: "Loading"
                    Text(
                        text = title,
                        modifier = Modifier
                            .padding(end = 8.dp, bottom = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading)
                            .combinedClickable(
                                onLongClick = {
                                    title.let { context.copyToClipBoard(it) }
                                },
                                onClick = { }
                            ),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val formatText = if (uiState.isExternal) "${uiState.mediaType.localized()} • ${uiState.externalReleaseDate?.take(4) ?: "N/A"}" else uiState.mediaDetails?.mediaFormatWithYear() ?: "Loading"
                    val formatIcon = if (uiState.mediaType == MediaType.GAMES) R.drawable.ic_round_casino_24 else if (uiState.isExternal) R.drawable.ic_round_movie_24 else if (uiState.isAnime) R.drawable.ic_round_local_movies_24 else R.drawable.ic_round_book_24

                    TextIconHorizontal(
                        text = formatText,
                        icon = formatIcon,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .defaultPlaceholder(visible = uiState.isLoading)
                    )

                    if (!uiState.isExternal) {
                        TextIconHorizontal(
                            text = uiState.mediaDetails?.status?.localized() ?: "Loading",
                            icon = if (uiState.isAnime) R.drawable.ic_round_rss_feed_24
                            else R.drawable.round_drive_file_rename_outline_24,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .defaultPlaceholder(visible = uiState.isLoading)
                        )
                        TextIconHorizontal(
                            text = uiState.mediaDetails?.durationText() ?: "Loading",
                            icon = if (uiState.isAnime) R.drawable.ic_round_timer_24
                            else R.drawable.ic_round_menu_book_24,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .defaultPlaceholder(visible = uiState.isLoading)
                        )
                        if (uiState.mediaDetails is MangaDetails && uiState.mediaDetails.hasVolumes) {
                            TextIconHorizontal(
                                text = uiState.mediaDetails.volumesText(),
                                icon = R.drawable.round_bookmark_24,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .defaultPlaceholder(visible = uiState.isLoading)
                            )
                        }
                    }

                    if (!uiState.hideScore) {
                        val scoreText = if (uiState.isExternal) uiState.externalScore?.let { String.format("%.1f", it) } ?: "N/A" else uiState.mediaDetails?.mean.toStringOrNull() ?: "??"
                        TextIconHorizontal(
                            text = scoreText,
                            icon = R.drawable.ic_round_details_star_24,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                }
            }//:Row

            val genresList = if (uiState.isExternal) uiState.externalGenres else uiState.mediaDetails?.genres?.map { it.localized() }.orEmpty()
            if (genresList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(bottom = 4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(genresList) { genreText ->
                        AssistChip(
                            onClick = { },
                            label = { Text(text = genreText) },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            //Synopsis
            val synopsisText = if (uiState.isExternal) uiState.externalSynopsis else uiState.mediaDetails?.synopsisAndBackground()?.toString()
            if (uiState.isLoading || !synopsisText.isNullOrEmpty()) {
                SelectionContainer {
                    Text(
                        text = synopsisText ?: stringResource(R.string.lorem_ipsun),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                            .animateContentSize()
                            .defaultPlaceholder(visible = uiState.isLoading),
                        lineHeight = 20.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = maxLinesSynopsis
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCurrentLanguageEn == false && !uiState.isExternal) {
                        IconButton(
                            onClick = {
                                uiState.mediaDetails?.synopsis?.let { context.openTranslator(it) }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_outline_translate_24),
                                contentDescription = stringResource(R.string.translate)
                            )
                        }
                    } else Spacer(modifier = Modifier.size(48.dp))

                    IconButton(
                        onClick = { isSynopsisExpanded = !isSynopsisExpanded }
                    ) {
                        Icon(painter = painterResource(iconExpand), contentDescription = "expand")
                    }

                    IconButton(
                        onClick = {
                            val textToCopy = if (uiState.isExternal) uiState.externalSynopsis else uiState.mediaDetails?.synopsis
                            textToCopy?.let { context.copyToClipBoard(it) }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_content_copy_24),
                            contentDescription = stringResource(R.string.copied)
                        )
                    }
                }
            }

            if (uiState.isExternal) {
                InfoTitle(text = stringResource(R.string.more_info))

                uiState.externalRuntime?.let {
                    MediaInfoView(title = stringResource(R.string.duration), info = it, modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading))
                }
                uiState.externalReleaseDate?.let {
                    MediaInfoView(title = stringResource(R.string.start_date), info = it, modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading))
                }
                uiState.externalStatus?.let {
                    MediaInfoView(title = "Status", info = it, modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading))
                }

                uiState.externalStudios?.takeIf { it.isNotBlank() }?.let {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SelectionContainer {
                        MediaInfoView(
                            title = if (uiState.mediaType == MediaType.GAMES) "Developer" else "Studio",
                            info = it,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            } else {
                //Stats (MAL Only)
                InfoTitle(text = stringResource(R.string.stats))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .defaultPlaceholder(visible = uiState.isLoading),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextIconVertical(
                        text = uiState.mediaDetails?.rankText().orEmpty(),
                        icon = R.drawable.ic_round_bar_chart_24,
                        tooltip = stringResource(R.string.top_ranked)
                    )
                    VerticalDivider(modifier = Modifier.height(32.dp))

                    TextIconVertical(
                        text = uiState.mediaDetails?.numScoringUsers?.format() ?: UNKNOWN_CHAR,
                        icon = R.drawable.ic_round_thumbs_up_down_24,
                        tooltip = stringResource(R.string.users_scores)
                    )
                    VerticalDivider(modifier = Modifier.height(32.dp))

                    TextIconVertical(
                        text = uiState.mediaDetails?.numListUsers?.format() ?: UNKNOWN_CHAR,
                        icon = R.drawable.ic_round_group_24,
                        tooltip = stringResource(R.string.members)
                    )
                    VerticalDivider(modifier = Modifier.height(32.dp))

                    TextIconVertical(
                        text = "# ${uiState.mediaDetails?.popularity}",
                        icon = R.drawable.ic_round_trending_up_24,
                        tooltip = stringResource(R.string.popularity)
                    )
                }//:Row

                //Info
                InfoTitle(text = stringResource(R.string.more_info))
                if (uiState.mediaDetails is AnimeDetails) {
                    MediaInfoView(
                        title = stringResource(R.string.duration),
                        info = uiState.mediaDetails.episodeDurationLocalized(),
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                } else if (uiState.mediaDetails is MangaDetails) {
                    SelectionContainer {
                        MediaInfoView(
                            title = stringResource(R.string.authors),
                            info = uiState.mediaDetails.authors
                                ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    SelectionContainer {
                        MediaInfoView(
                            title = stringResource(R.string.serialization),
                            info = uiState.serializationJoined,
                            modifier = Modifier
                                .defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                MediaInfoView(
                    title = stringResource(R.string.start_date),
                    info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
                MediaInfoView(
                    title = stringResource(R.string.end_date),
                    info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                    modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                )
                if (uiState.mediaDetails is AnimeDetails) {
                    MediaInfoView(
                        title = stringResource(R.string.season),
                        info = uiState.mediaDetails.startSeason?.seasonYearText(),
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                    MediaInfoView(
                        title = stringResource(R.string.broadcast),
                        info = uiState.mediaDetails.broadcast?.timeText(
                            isAiring = uiState.mediaDetails.status == MediaStatus.AIRING
                        ),
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                    MediaInfoView(
                        title = stringResource(R.string.source),
                        info = uiState.mediaDetails.source?.localized(),
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                if (uiState.mediaDetails is AnimeDetails) {
                    SelectionContainer {
                        MediaInfoView(
                            title = stringResource(R.string.studios),
                            info = uiState.studiosJoined,
                            modifier = Modifier
                                .defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                uiState.mediaDetails?.synonymsJoined()?.let { synonyms ->
                    SelectionContainer {
                        MediaInfoView(
                            title = stringResource(R.string.synonyms),
                            info = synonyms,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                }
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.jp_title),
                        info = uiState.mediaDetails?.alternativeTitles?.ja,
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.romaji),
                        info = uiState.mediaDetails?.title,
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                }
                SelectionContainer {
                    MediaInfoView(
                        title = stringResource(R.string.english),
                        info = uiState.mediaDetails?.alternativeTitles?.en,
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                }

                //Characters
                if (uiState.isAnime) {
                    var showCharacters by remember { mutableStateOf(false) }

                    InfoTitle(text = stringResource(R.string.characters))
                    if (uiState.characters.isNotEmpty() || uiState.isLoadingCharacters) {
                        LazyRow(
                            modifier = Modifier.padding(top = 8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(
                                items = uiState.characters,
                                contentType = { it }
                            ) { item ->
                                MediaItemVertical(
                                    imageUrl = item.node.mainPicture?.medium,
                                    title = item.fullName(),
                                    modifier = Modifier.padding(end = 8.dp),
                                    subtitle = {
                                        Text(
                                            text = item.role?.localized().orEmpty(),
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 13.sp
                                        )
                                    },
                                    minLines = 2,
                                    onClick = {
                                        context.openLink(CHARACTER_URL + item.node.id)
                                    }
                                )
                            }
                            if (uiState.isLoadingCharacters) {
                                item {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    } else {
                        TextButton(
                            onClick = {
                                showCharacters = true
                                event?.getCharacters()
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(text = stringResource(R.string.view_characters))
                        }
                    }
                }

                //Themes
                var showMusicSheet by remember { mutableStateOf(false) }
                var selectedSong by remember { mutableStateOf<String?>(null) }

                if (showMusicSheet && selectedSong != null) {
                    MusicStreamingSheet(
                        songTitle = selectedSong.orEmpty(),
                        bottomPadding = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                        onDismiss = {
                            showMusicSheet = false
                            selectedSong = null
                        }
                    )
                }
                if (uiState.mediaDetails is AnimeDetails) {
                    uiState.mediaDetails.openingThemes?.let { themes ->
                        InfoTitle(text = stringResource(R.string.opening))
                        themes.forEach { theme ->
                            AnimeThemeItem(
                                text = theme.text,
                                onClick = {
                                    selectedSong = theme.text
                                    showMusicSheet = true
                                }
                            )
                        }
                    }

                    uiState.mediaDetails.endingThemes?.let { themes ->
                        InfoTitle(text = stringResource(R.string.ending))
                        themes.forEach { theme ->
                            AnimeThemeItem(
                                text = theme.text,
                                onClick = {
                                    selectedSong = theme.text
                                    showMusicSheet = true
                                }
                            )
                        }
                    }
                }

                //Related
                if (uiState.relatedAnime.isNotEmpty()) {
                    InfoTitle(text = stringResource(R.string.related_anime))
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(uiState.relatedAnime) { item ->
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.large,
                                title = item.node.userPreferredTitle(),
                                modifier = Modifier.padding(end = 8.dp),
                                subtitle = {
                                    Text(
                                        text = item.relationType.localized(),
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 13.sp,
                                        lineHeight = 14.sp
                                    )
                                },
                                onClick = dropUnlessResumed {
                                    navActionManager.toMediaDetails(MediaType.ANIME, item.node.id)
                                }
                            )
                        }
                    }
                }
                if (uiState.relatedManga.isNotEmpty()) {
                    InfoTitle(text = stringResource(R.string.related_manga))
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(uiState.relatedManga) { item ->
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.large,
                                title = item.node.userPreferredTitle(),
                                modifier = Modifier.padding(end = 8.dp),
                                subtitle = {
                                    Text(
                                        text = item.relationType.localized(),
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 13.sp,
                                        lineHeight = 14.sp
                                    )
                                },
                                onClick = dropUnlessResumed {
                                    navActionManager.toMediaDetails(MediaType.MANGA, item.node.id)
                                }
                            )
                        }
                    }
                }

                //Recommendations
                if (uiState.recommendations.isNotEmpty()) {
                    InfoTitle(text = stringResource(R.string.recommendations))
                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(uiState.recommendations) { item ->
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.large,
                                title = item.node.userPreferredTitle(),
                                modifier = Modifier.padding(end = 8.dp),
                                subtitle = {
                                    TextIconHorizontal(
                                        text = item.numRecommendations.format() ?: UNKNOWN_CHAR,
                                        icon = R.drawable.ic_round_thumbs_up_down_16,
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 13.sp,
                                        iconSize = 20.dp,
                                    )
                                },
                                minLines = 2,
                                onClick = dropUnlessResumed {
                                    navActionManager.toMediaDetails(
                                        mediaType = item.node.mediaType,
                                        id = item.node.id
                                    )
                                }
                            )
                        }
                    }
                }

                (uiState.mediaDetails as? AnimeDetails)?.statistics?.status?.toStats()?.let { stats ->
                    InfoTitle(text = stringResource(R.string.status_distribution))
                    HorizontalStatsBar(
                        stats = stats
                    )
                }
            }
        }//:Column
    }//:Scaffold
}

@Preview
@Composable
fun MediaDetailsPreview() {
    MoeListTheme {
        Surface {
            MediaDetailsContent(
                uiState = MediaDetailsUiState(),
                event = null,
                isLoggedIn = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}