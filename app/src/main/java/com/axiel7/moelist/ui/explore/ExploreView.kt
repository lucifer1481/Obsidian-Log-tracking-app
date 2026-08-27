package com.axiel7.moelist.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.media.MediaPoster
import org.koin.androidx.compose.koinViewModel
import com.axiel7.moelist.ui.composables.HeaderHorizontalList

@Composable
fun ExploreView(
    navActionManager: NavActionManager,
    viewModel: ExploreViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 88.dp) // Accounts for bottom nav bar
            ) {

                // TRENDING MOVIES
                if (uiState.trendingMovies.isNotEmpty()) {
                    item {
                        HeaderHorizontalList(
                            text = "Trending Movies",
                            onClick = dropUnlessResumed { navActionManager.toExploreCategory("MOVIES") }
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            items(uiState.trendingMovies) { movie ->
                                ExploreMediaItem(
                                    title = movie.title ?: "Unknown",
                                    posterPath = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                                    onClick = dropUnlessResumed {
                                        navActionManager.toMediaDetails(MediaType.MOVIES, movie.id.toInt())
                                    }
                                )
                            }
                        }
                    }
                }

                // POPULAR SERIES
                if (uiState.popularSeries.isNotEmpty()) {
                    item {
                        HeaderHorizontalList(
                            text = "Popular Series",
                            onClick = dropUnlessResumed { navActionManager.toExploreCategory("SERIES") })
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            items(uiState.popularSeries) { series ->
                                ExploreMediaItem(
                                    title = series.name ?: "Unknown",
                                    posterPath = series.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                                    onClick = dropUnlessResumed {
                                        navActionManager.toMediaDetails(MediaType.SERIES, series.id.toInt())
                                    }
                                )
                            }
                        }
                    }
                }

                // TRENDING GAMES
                if (uiState.trendingGames.isNotEmpty()) {
                    item {
                        HeaderHorizontalList(
                            text = "Most Anticipated Games",
                            onClick = dropUnlessResumed { navActionManager.toExploreCategory("GAMES") }
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            items(uiState.trendingGames) { game ->
                                ExploreMediaItem(
                                    title = game.name,
                                    posterPath = game.backgroundImage,
                                    onClick = dropUnlessResumed {
                                        navActionManager.toMediaDetails(MediaType.GAMES, game.id.toInt())
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreMediaItem(
    title: String,
    posterPath: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        MediaPoster(
            url = posterPath,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .padding(bottom = 8.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}