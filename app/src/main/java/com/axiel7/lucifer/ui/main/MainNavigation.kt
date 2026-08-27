package com.axiel7.lucifer.ui.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.navigation.NavActionManager
import com.axiel7.lucifer.ui.base.navigation.Route
import com.axiel7.lucifer.ui.calendar.CalendarView
import com.axiel7.lucifer.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.lucifer.ui.custommedia.CustomMediaListView
import com.axiel7.lucifer.ui.details.MediaDetailsView
import com.axiel7.lucifer.ui.explore.ExploreCategoryView
import com.axiel7.lucifer.ui.explore.ExploreView
import com.axiel7.lucifer.ui.fullposter.FullPosterView
import com.axiel7.lucifer.ui.home.HomeView
import com.axiel7.lucifer.ui.login.LoginView
import com.axiel7.lucifer.ui.login.SupabaseLoginView
import com.axiel7.lucifer.ui.more.MoreView
import com.axiel7.lucifer.ui.more.about.AboutView
import com.axiel7.lucifer.ui.more.credits.CreditsView
import com.axiel7.lucifer.ui.more.notifications.NotificationsView
import com.axiel7.lucifer.ui.more.settings.SettingsView
import com.axiel7.lucifer.ui.more.settings.list.ListStyleSettingsView
import com.axiel7.lucifer.ui.profile.ProfileView
import com.axiel7.lucifer.ui.ranking.MediaRankingView
import com.axiel7.lucifer.ui.recommendations.RecommendationsView
import com.axiel7.lucifer.ui.search.SearchHostView
import com.axiel7.lucifer.ui.season.SeasonChartView
import com.axiel7.lucifer.ui.userlist.UserMediaListWithFabView
import com.axiel7.lucifer.ui.userlist.UserMediaListWithTabsView
import kotlin.reflect.typeOf

@Composable
fun MainNavigation(
    navController: NavHostController,
    navActionManager: NavActionManager,
    lastTabOpened: Int,
    isLoggedIn: Boolean,
    isCompactScreen: Boolean,
    useListTabs: Boolean,
    modifier: Modifier,
    padding: PaddingValues,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash,
        modifier = modifier,
        enterTransition = {
            fadeIn(
                animationSpec = tween(250, easing = LinearEasing)
            ) + slideIntoContainer(
                animationSpec = tween(250, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(300, easing = LinearEasing)
            ) + slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(250, easing = LinearEasing)
            )
        },
    ) {
        composable<Route.Tab.Home>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            HomeView(
                isLoggedIn = isLoggedIn,
                navActionManager = navActionManager,
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }
        composable<Route.Splash> {
            com.axiel7.lucifer.ui.splash.SplashView(
                onSplashFinished = {
                    navController.navigate(Route.Tab.Home) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Tab.Anime>(
            typeMap = mapOf(typeOf<MediaType>() to MediaType.navType),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            if (!isLoggedIn) {
                LoginView()
            } else {
                if (useListTabs) {
                    UserMediaListWithTabsView(
                        mediaType = MediaType.ANIME,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        padding = padding
                    )
                } else {
                    UserMediaListWithFabView(
                        mediaType = MediaType.ANIME,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                }
            }
        }

        composable<Route.Tab.Manga>(
            typeMap = mapOf(typeOf<MediaType>() to MediaType.navType),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            if (!isLoggedIn) {
                LoginView()
            } else {
                if (useListTabs) {
                    UserMediaListWithTabsView(
                        mediaType = MediaType.MANGA,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        padding = padding
                    )
                } else {
                    UserMediaListWithFabView(
                        mediaType = MediaType.MANGA,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                }
            }
        }

        composable<Route.Tab.More>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            MoreView(
                navActionManager = navActionManager,
                padding = padding,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
                isLoggedIn = isLoggedIn
            )
        }

        composable<Route.MediaRanking>(
            typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
        ) {
            val args = it.toRoute<Route.MediaRanking>()

            MediaRankingView(
                mediaType = args.mediaType,
                isCompactScreen = isCompactScreen,
                navActionManager = navActionManager,
            )
        }

        composable<Route.Calendar> {
            CalendarView(
                navActionManager = navActionManager
            )
        }

        composable<Route.SeasonChart> {
            SeasonChartView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Recommendations> {
            RecommendationsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Settings> {
            SettingsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.ListStyleSettings> {
            ListStyleSettingsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Notifications> {
            NotificationsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.About> {
            AboutView(
                navActionManager = navActionManager
            )
        }

        composable<Route.Credits> {
            CreditsView(
                navActionManager = navActionManager
            )
        }

        composable<Route.MediaDetails>(
            typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
        ) {
            MediaDetailsView(
                isLoggedIn = isLoggedIn,
                navActionManager = navActionManager
            )
        }

        composable<Route.FullPoster>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            val args = it.toRoute<Route.FullPoster>()

            FullPosterView(
                pictures = args.pictures,
                navActionManager = navActionManager
            )
        }

        composable<Route.Profile> {
            if (!isLoggedIn) {
                DefaultScaffoldWithTopAppBar(
                    title = stringResource(R.string.title_profile),
                    navigateBack = { navController.popBackStack() }
                ) { padding ->
                    LoginView(modifier = Modifier.padding(padding))
                }
            } else {
                ProfileView(
                    navActionManager = navActionManager
                )
            }
        }

        // 🚀 LOCKED DOWN: Movies Tab requires Supabase Auth
        composable<Route.Tab.Movies>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            if (!isLoggedIn) {
                SupabaseLoginView(
                    onLoginSuccess = { /* Handle success state */ }
                )
            } else {
                if (useListTabs) {
                    UserMediaListWithTabsView(
                        mediaType = MediaType.MOVIES,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        padding = padding
                    )
                } else {
                    UserMediaListWithFabView(
                        mediaType = MediaType.MOVIES,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                }
            }
        }

        composable<Route.ExploreCategory> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.ExploreCategory>()
            ExploreCategoryView(
                category = args.category,
                navActionManager = navActionManager,
                padding = padding
            )
        }

        // 🚀 LOCKED DOWN: Series Tab requires Supabase Auth
        composable<Route.Tab.Series>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            if (!isLoggedIn) {
                SupabaseLoginView(
                    onLoginSuccess = { /* Handle success state */ }
                )
            } else {
                if (useListTabs) {
                    UserMediaListWithTabsView(
                        mediaType = MediaType.SERIES,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        padding = padding
                    )
                } else {
                    UserMediaListWithFabView(
                        mediaType = MediaType.SERIES,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                }
            }
        }

        // 🚀 LOCKED DOWN: Games Tab requires Supabase Auth
        composable<Route.Tab.Games>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            if (!isLoggedIn) {
                SupabaseLoginView(
                    onLoginSuccess = { /* Handle success state */ }
                )
            } else {
                if (useListTabs) {
                    UserMediaListWithTabsView(
                        mediaType = MediaType.GAMES,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        padding = padding
                    )
                } else {
                    UserMediaListWithFabView(
                        mediaType = MediaType.GAMES,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        topBarHeightPx = topBarHeightPx,
                        topBarOffsetY = topBarOffsetY,
                        padding = padding
                    )
                }
            }
        }

        composable<Route.Search>(
            typeMap = mapOf(typeOf<MediaType>() to MediaType.navType),
            enterTransition = {
                expandVertically(expandFrom = Alignment.Top)
            },
            exitTransition = {
                shrinkVertically(shrinkTowards = Alignment.Top)
            },
            popEnterTransition = {
                expandVertically(expandFrom = Alignment.Top)
            },
            popExitTransition = {
                shrinkVertically(shrinkTowards = Alignment.Top)
            },
        ) {
            SearchHostView(
                isCompactScreen = isCompactScreen,
                padding = if (isCompactScreen) PaddingValues() else padding,
                navActionManager = navActionManager
            )
        }
        composable<Route.Explore> {
            ExploreView(
                navActionManager = navActionManager
            )
        }

        composable<Route.CustomMedia> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.CustomMedia>()
            CustomMediaListView(
                mediaType = route.mediaType,
                title = route.title,
                padding = padding
            )
        }
    }
}