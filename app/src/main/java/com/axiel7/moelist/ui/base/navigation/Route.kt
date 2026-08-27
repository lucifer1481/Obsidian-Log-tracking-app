package com.axiel7.moelist.ui.base.navigation

import com.axiel7.moelist.data.model.media.MediaType
import kotlinx.serialization.Serializable

sealed interface Route {
    sealed interface Tab : Route {
        @Serializable
        data object Home : Tab

        @Serializable
        data class Anime(val mediaType: MediaType) : Tab

        @Serializable
        data class Manga(val mediaType: MediaType) : Tab

        // 🚀 NEW: Distinct, official tab routes so Navigation doesn't confuse them!
        @Serializable
        data object Movies : Tab

        @Serializable
        data object Series : Tab

        @Serializable
        data object Games : Tab

        @Serializable
        data object More : Tab
    }

    @Serializable
    data class CustomMedia(val mediaType: String, val title: String) : Route

    @Serializable
    data class MediaRanking(val mediaType: MediaType) : Route

    @Serializable
    data class MediaDetails(
        val mediaType: MediaType,
        val mediaId: Int,
    ) : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object SeasonChart : Route

    @Serializable
    data object Recommendations : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class Search(
        val mediaType: MediaType = MediaType.ANIME
    ) : Route

    @Serializable
    data class FullPoster(val pictures: List<String>) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object ListStyleSettings : Route
    @Serializable
    data object Splash : Route
    @Serializable
    data object Explore : Route
    @Serializable
    data class ExploreCategory(val category: String) : Route
    @Serializable
    data object Notifications : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Credits : Route
}