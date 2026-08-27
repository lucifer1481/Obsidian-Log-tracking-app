package com.axiel7.lucifer.ui.base.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.axiel7.lucifer.data.model.media.MediaType

@Immutable
class NavActionManager(
    private val navController: NavHostController
) {
    fun goBack() {
        navController.popBackStack()
    }

    fun toMediaRanking(mediaType: MediaType) {
        navController.navigate(Route.MediaRanking(mediaType))
    }

    fun toMediaDetails(mediaType: MediaType, id: Int) {
        navController.navigate(
            Route.MediaDetails(
                mediaType = mediaType,
                mediaId = id,
            )
        )
    }

    fun toCalendar() {
        navController.navigate(Route.Calendar)
    }

    fun toSeasonChart() {
        navController.navigate(Route.SeasonChart)
    }

    fun toRecommendations() {
        navController.navigate(Route.Recommendations)
    }

    fun toFullPoster(pictures: List<String>) {
        navController.navigate(Route.FullPoster(pictures))
    }

    fun toSettings() {
        navController.navigate(Route.Settings)
    }

    fun toListStyleSettings() {
        navController.navigate(Route.ListStyleSettings)
    }

    fun toNotifications() {
        navController.navigate(Route.Notifications)
    }

    fun toAbout() {
        navController.navigate(Route.About)
    }

    fun toCredits() {
        navController.navigate(Route.Credits)
    }
    fun toExplore() {
        navController.navigate(Route.Explore)
    }
    fun toExploreCategory(category: String) {
        navController.navigate(Route.ExploreCategory(category))
    }
    fun toUserList(mediaType: MediaType) {
        val destination = when (mediaType) {
            MediaType.ANIME -> Route.Tab.Anime(mediaType)
            MediaType.MANGA -> Route.Tab.Manga(mediaType)
            MediaType.MOVIES -> Route.Tab.Movies
            MediaType.SERIES -> Route.Tab.Series
            MediaType.GAMES -> Route.Tab.Games
        }
        navController.navigate(destination)
    }

    companion object {
        @Composable
        fun rememberNavActionManager(
            navController: NavHostController = rememberNavController()
        ) = remember {
            NavActionManager(navController)
        }
    }
}
