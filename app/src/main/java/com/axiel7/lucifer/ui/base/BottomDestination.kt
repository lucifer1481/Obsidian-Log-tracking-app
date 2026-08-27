package com.axiel7.lucifer.ui.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.navigation.Route

sealed class BottomDestination(
    val value: String,
    val route: Any,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val iconSelected: Int,
) {
    data object Home : BottomDestination(
        value = "home",
        route = Route.Tab.Home,
        title = R.string.title_home,
        icon = R.drawable.ic_outline_home_24,
        iconSelected = R.drawable.ic_round_home_24
    )

    data object AnimeList : BottomDestination(
        value = "anime",
        route = Route.Tab.Anime(mediaType = MediaType.ANIME),
        title = R.string.title_anime_list,
        icon = R.drawable.ic_outline_local_movies_24,
        iconSelected = R.drawable.ic_round_local_movies_24
    )

    data object MangaList : BottomDestination(
        value = "manga",
        route = Route.Tab.Manga(MediaType.MANGA),
        title = R.string.title_manga_list,
        icon = R.drawable.ic_outline_book_24,
        iconSelected = R.drawable.ic_round_book_24
    )

    // 🚀 FIXED: Pointing to their own unique Top-Level Tab routes
    data object MoviesList : BottomDestination(
        value = "movies",
        route = Route.Tab.Movies,
        title = R.string.title_anime_list,
        icon = R.drawable.ic_round_movie_24,
        iconSelected = R.drawable.ic_round_movie_24
    )

    data object SeriesList : BottomDestination(
        value = "series",
        route = Route.Tab.Series,
        title = R.string.title_anime_list,
        icon = R.drawable.ic_round_local_movies_24,
        iconSelected = R.drawable.ic_round_local_movies_24
    )

    data object GamesList : BottomDestination(
        value = "games",
        route = Route.Tab.Games,
        title = R.string.title_anime_list,
        icon = R.drawable.ic_round_casino_24,
        iconSelected = R.drawable.ic_round_casino_24
    )

    data object Profile : BottomDestination(
        value = "profile",
        route = Route.Profile,
        title = R.string.title_profile,
        icon = R.drawable.ic_outline_person_24,
        iconSelected = R.drawable.ic_round_person_24
    )

    data object More : BottomDestination(
        value = "more",
        route = Route.Tab.More,
        title = R.string.more,
        icon = R.drawable.ic_more_horizontal,
        iconSelected = R.drawable.ic_more_horizontal
    )

    companion object {
        val values = listOf(Home, AnimeList, MangaList, MoviesList, SeriesList, GamesList)

        val railValues = listOf(Home, AnimeList, MangaList, Profile, More)

        fun String.toBottomDestinationIndex() = when (this) {
            Home.value -> 0
            AnimeList.value -> 1
            MangaList.value -> 2
            MoviesList.value -> 3
            SeriesList.value -> 4
            GamesList.value -> 5
            else -> null
        }

        fun NavBackStackEntry.isBottomDestination() =
            destination.hierarchy.any { dest ->
                values.any { value -> dest.hasRoute(value.route::class) }
            }

        @Composable
        fun BottomDestination.Icon(selected: Boolean) {
            androidx.compose.material3.Icon(
                painter = painterResource(if (selected) iconSelected else icon),
                contentDescription = stringResource(title)
            )
        }
    }
}