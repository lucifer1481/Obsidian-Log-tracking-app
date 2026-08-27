package com.axiel7.lucifer.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import coil.compose.AsyncImage
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.media.BaseMediaNode
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.navigation.NavActionManager
import com.axiel7.lucifer.ui.composables.HeaderHorizontalList
import com.axiel7.lucifer.ui.composables.collapsable
import com.axiel7.lucifer.ui.composables.media.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.lucifer.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.lucifer.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.lucifer.ui.composables.media.MediaItemVertical
import com.axiel7.lucifer.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.lucifer.ui.composables.score.SmallScoreIndicator
import com.axiel7.lucifer.ui.home.composables.AiringAnimeHorizontalItem
import com.axiel7.lucifer.ui.home.composables.HomeCard
import com.axiel7.lucifer.ui.theme.MoeListTheme
import com.axiel7.lucifer.utils.ContextExtensions.showToast
import com.axiel7.lucifer.utils.SeasonCalendar
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random

@Composable
fun HomeView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeViewContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
        topBarHeightPx = topBarHeightPx,
        topBarOffsetY = topBarOffsetY,
        padding = padding,
    )
}

@Composable
private fun HomeViewContent(
    uiState: HomeUiState,
    event: HomeEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Animatable<Float, AnimationVector1D> = Animatable(0f),
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val airingListState = rememberLazyListState()
    val seasonListState = rememberLazyListState()
    val recommendListState = rememberLazyListState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    LaunchedEffect(isLoggedIn) {
        event?.initRequestChain(isLoggedIn)
    }

    Column(
        modifier = Modifier
            .collapsable(
                state = scrollState,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
            .verticalScroll(scrollState)
            .padding(bottom = padding.calculateBottomPadding())
    ) {

        // ==========================================
        // 🚀 THE PREMIUM HERO BANNER (6-Item Slider)
        // ==========================================
        if (!uiState.isLoading && uiState.seasonAnimes.isNotEmpty()) {
            val heroAnimes = uiState.seasonAnimes.take(6)
            val pagerState = rememberPagerState(pageCount = { heroAnimes.size })

            HorizontalPager(state = pagerState) { page ->
                val featuredAnime = heroAnimes[page].node
                HeroBanner(
                    anime = featuredAnime,
                    onDetailsClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(MediaType.ANIME, featuredAnime.id)
                    }
                )
            }
        } else if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .background(Color.DarkGray.copy(alpha = 0.3f))
            )
        }

        // ==========================================
        // 🚀 SLEEK QUICK ACTIONS ROW (With New Categories)
        // ==========================================
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HomeCard(text = stringResource(R.string.anime_ranking), icon = R.drawable.ic_round_movie_24, modifier = Modifier.width(140.dp), onClick = dropUnlessResumed { navActionManager.toMediaRanking(MediaType.ANIME) })
            }
            item {
                HomeCard(text = stringResource(R.string.manga_ranking), icon = R.drawable.ic_round_menu_book_24, modifier = Modifier.width(140.dp), onClick = dropUnlessResumed { navActionManager.toMediaRanking(MediaType.MANGA) })
            }

            // 🚀 FIXED: These buttons now navigate directly to your Supabase User Library tabs!
            // 🚀 FIXED: These buttons now navigate directly to your new Explore Page!
            item {
                HomeCard(
                    text = "Explore M,s,g",
                    icon = R.drawable.ic_round_local_movies_24,
                    modifier = Modifier.width(140.dp),
                    onClick = dropUnlessResumed { navActionManager.toExplore() }
                )
            }


            item {
                HomeCard(text = stringResource(R.string.seasonal_chart), icon = SeasonCalendar.currentSeason.icon, modifier = Modifier.width(140.dp), onClick = dropUnlessResumed { navActionManager.toSeasonChart() })
            }
            item {
                HomeCard(text = stringResource(R.string.calendar), icon = R.drawable.ic_round_event_24, modifier = Modifier.width(140.dp), onClick = dropUnlessResumed { navActionManager.toCalendar() })
            }
        }

        // ==========================================
        // HORIZONTAL LIST: AIRING TODAY
        // ==========================================
        HeaderHorizontalList(text = "Trending Today", onClick = dropUnlessResumed { navActionManager.toCalendar() })
        if (!uiState.isLoading && uiState.todayAnimes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(MEDIA_POSTER_SMALL_HEIGHT.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.nothing_today), textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else LazyRow(
            modifier = Modifier.padding(top = 4.dp).sizeIn(minHeight = MEDIA_POSTER_SMALL_HEIGHT.dp),
            state = airingListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState)
        ) {
            items(items = uiState.todayAnimes, key = { it.node.id }, contentType = { it.node }) {
                AiringAnimeHorizontalItem(
                    item = it, hideScore = uiState.hideScore,
                    onClick = dropUnlessResumed { navActionManager.toMediaDetails(MediaType.ANIME, it.node.id) }
                )
            }
            if (uiState.isLoading) items(5) { MediaItemDetailedPlaceholder() }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // HORIZONTAL LIST: THIS SEASON
        // ==========================================
        HeaderHorizontalList(text = "Popular This Season", onClick = dropUnlessResumed { navActionManager.toSeasonChart() })
        if (!uiState.isLoading && uiState.seasonAnimes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(MEDIA_POSTER_SMALL_HEIGHT.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.error_server), textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else LazyRow(
            modifier = Modifier.padding(top = 4.dp).sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            state = seasonListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = seasonListState)
        ) {
            // Drops the top 6 so they don't repeat right under the hero slider!
            val displayList = if (uiState.seasonAnimes.size > 6) uiState.seasonAnimes.drop(6) else emptyList()

            items(items = displayList, key = { it.node.id }, contentType = { it.node }) {
                MediaItemVertical(
                    imageUrl = it.node.mainPicture?.large,
                    title = it.node.userPreferredTitle(),
                    modifier = Modifier.padding(end = 12.dp),
                    badgeContent = it.node.myListStatus?.status?.let { status ->
                        { Icon(painter = painterResource(status.icon), contentDescription = status.localized(), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                    },
                    subtitle = if (!uiState.hideScore) { { SmallScoreIndicator(score = it.node.mean, fontSize = 13.sp) } } else null,
                    minLines = 2,
                    onClick = dropUnlessResumed { navActionManager.toMediaDetails(MediaType.ANIME, it.node.id) }
                )
            }
            if (uiState.isLoading) items(10) { MediaItemVerticalPlaceholder() }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // HORIZONTAL LIST: RECOMMENDATIONS
        // ==========================================
        HeaderHorizontalList(text = "Because You Watched...", onClick = dropUnlessResumed { navActionManager.toRecommendations() })
        if (!isLoggedIn) {
            Box(modifier = Modifier.fillMaxWidth().height(MEDIA_POSTER_SMALL_HEIGHT.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.please_login_to_use_this_feature), textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else if (!uiState.isLoading && uiState.recommendedAnimes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(MEDIA_POSTER_SMALL_HEIGHT.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.no_recommendations), textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else LazyRow(
            modifier = Modifier.padding(top = 4.dp).sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            state = recommendListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = recommendListState)
        ) {
            items(items = uiState.recommendedAnimes, key = { it.node.id }, contentType = { it.node }) {
                MediaItemVertical(
                    imageUrl = it.node.mainPicture?.large,
                    title = it.node.userPreferredTitle(),
                    modifier = Modifier.padding(end = 12.dp),
                    subtitle = if (!uiState.hideScore) { { SmallScoreIndicator(score = it.node.mean, fontSize = 13.sp) } } else null,
                    minLines = 2,
                    onClick = dropUnlessResumed { navActionManager.toMediaDetails(MediaType.ANIME, it.node.id) }
                )
            }
            if (uiState.isLoading) items(10) { MediaItemVerticalPlaceholder() }
        }

        // ==========================================
        // RANDOM BUTTON
        // ==========================================
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalArrangement = Arrangement.Center) {
            OutlinedButton(
                onClick = dropUnlessResumed {
                    val type = if (Random.nextBoolean()) MediaType.ANIME else MediaType.MANGA
                    val id = Random.nextInt(from = 0, until = 6000)
                    navActionManager.toMediaDetails(type, id)
                }
            ) {
                Icon(painter = painterResource(R.drawable.ic_round_casino_24), contentDescription = stringResource(R.string.random), modifier = Modifier.padding(end = 8.dp).size(18.dp))
                Text(text = "Surprise Me", overflow = TextOverflow.Ellipsis, maxLines = 1)
            }
        }
    }
}

// ==========================================
// 🎨 UPDATED HERO BANNER
// ==========================================
@Composable
fun HeroBanner(anime: BaseMediaNode, onDetailsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .background(Color.Black)
    ) {
        AsyncImage(
            model = anime.mainPicture?.large ?: anime.mainPicture?.medium,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = anime.userPreferredTitle(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray.copy(alpha = 0.8f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_info), contentDescription = "Details", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    MoeListTheme {
        Surface {
            HomeViewContent(
                uiState = HomeUiState(),
                event = null,
                isLoggedIn = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}