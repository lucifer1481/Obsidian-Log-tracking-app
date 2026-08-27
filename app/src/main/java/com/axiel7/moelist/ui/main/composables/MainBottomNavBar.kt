package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.BottomDestination.Companion.Icon
import com.axiel7.moelist.ui.base.navigation.Route
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainBottomNavBar(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?,
    isVisible: Boolean,
    onItemSelected: (Int) -> Unit,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) {
    val scope = rememberCoroutineScope()

    AnimatedContent(
        targetState = isVisible,
        transitionSpec = {
            slideInVertically(initialOffsetY = { it }) togetherWith
                    slideOutVertically(targetOffsetY = { it })
        }
    ) { isVisibleState ->
        if (isVisibleState) {
            NavigationBar {
                BottomDestination.values.forEachIndexed { index, dest ->
                    val currentRoute = navBackStackEntry?.destination?.route ?: ""
                    val isSelected = when (dest) {
                        is BottomDestination.MoviesList -> currentRoute.contains("MOVIES")
                        is BottomDestination.SeriesList -> currentRoute.contains("SERIES")
                        is BottomDestination.GamesList -> currentRoute.contains("GAMES")
                        is BottomDestination.AnimeList -> currentRoute.contains("ANIME")
                        is BottomDestination.MangaList -> currentRoute.contains("MANGA")
                        else -> navBackStackEntry?.destination?.hierarchy?.any {
                            if (dest.route is String) false else it.hasRoute(dest.route::class)
                        } == true
                    }

                    NavigationBarItem(
                        icon = { dest.Icon(selected = isSelected) },
                        label = {
                            val titleText = when (dest) {
                                is BottomDestination.MoviesList -> "Movies"
                                is BottomDestination.SeriesList -> "Series"
                                is BottomDestination.GamesList -> "Games"
                                else -> stringResource(dest.title)
                            }
                            Text(text = titleText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp)
                        },
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                // Double-tap shortcut to open Search for that specific category
                                when (dest) {
                                    BottomDestination.More -> navController.navigate(Route.Settings)
                                    is BottomDestination.MoviesList -> navController.navigate(Route.Search(mediaType = MediaType.MOVIES))
                                    is BottomDestination.SeriesList -> navController.navigate(Route.Search(mediaType = MediaType.SERIES))
                                    is BottomDestination.GamesList -> navController.navigate(Route.Search(mediaType = MediaType.GAMES))
                                    else -> {
                                        navController.navigate(Route.Search(
                                            mediaType = MediaType.MANGA.takeIf { dest == BottomDestination.MangaList } ?: MediaType.ANIME
                                        ))
                                    }
                                }
                            } else {
                                scope.launch { topBarOffsetY.animateTo(0f) }
                                onItemSelected(index)
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}