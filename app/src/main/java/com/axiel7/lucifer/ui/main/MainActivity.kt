package com.axiel7.lucifer.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.axiel7.lucifer.data.network.SupabaseApi.client
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.lucifer.App
import com.axiel7.lucifer.App.Companion.accessToken
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.BottomDestination.Companion.isBottomDestination
import com.axiel7.lucifer.ui.base.BottomDestination.Companion.toBottomDestinationIndex
import com.axiel7.lucifer.ui.base.TabletMode
import com.axiel7.lucifer.ui.base.ThemeStyle
import com.axiel7.lucifer.ui.base.navigation.NavActionManager
import com.axiel7.lucifer.ui.base.navigation.NavActionManager.Companion.rememberNavActionManager
import com.axiel7.lucifer.ui.main.composables.MainBottomNavBar
import com.axiel7.lucifer.ui.main.composables.MainNavigationRail
import com.axiel7.lucifer.ui.main.composables.MainTopAppBar
import com.axiel7.lucifer.ui.theme.MoeListTheme
import com.axiel7.lucifer.ui.theme.dark_scrim
import com.axiel7.lucifer.ui.theme.light_scrim
import com.axiel7.lucifer.utils.ContextExtensions.openLink
import com.axiel7.lucifer.utils.MOELIST_PAGELINK
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, KoinExperimentalAPI::class)
class MainActivity : AppCompatActivity() {

    val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🚀 Handle Supabase OAuth deep links on app launch
        client.handleDeeplinks(intent)

        lifecycleScope.launch {
            client.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    val token = client.auth.currentAccessTokenOrNull()
                    if (!token.isNullOrEmpty()) {
                        App.accessToken = token
                    }
                }
            }
        }

        checkLoginIntent(intent)

        App.accessToken = runBlocking { viewModel.accessToken.first() }

        runBlocking {
            App.titleLanguage = viewModel.titleLanguage.first()
        }

        val mediaIdAndType = findMediaIdAndTypeFromIntent()
        val mediaId = mediaIdAndType?.first
        val mediaTypeArg = mediaIdAndType?.second

        val lastTabOpened = findLastTabOpened()
        val initialTheme = runBlocking { viewModel.theme.first() }
        val initialUseBlackColors = runBlocking { viewModel.useBlackColors.first() }
        val initialTabletMode = runBlocking { viewModel.tabletMode.first() }

        setContent {
            KoinAndroidContext {
                val theme by viewModel.theme.collectAsStateWithLifecycle(initialValue = initialTheme)
                val useBlackColors by viewModel.useBlackColors.collectAsStateWithLifecycle(
                    initialValue = initialUseBlackColors
                )
                val isDark = if (theme == ThemeStyle.FOLLOW_SYSTEM) isSystemInDarkTheme()
                else theme == ThemeStyle.DARK

                val navController = rememberNavController()
                val navActionManager = rememberNavActionManager(navController)
                val navBackStackEntry by navController.currentBackStackEntryAsState()

                val tabletMode by viewModel.tabletMode.collectAsStateWithLifecycle(initialTabletMode)
                val windowSizeClass = calculateWindowSizeClass(this)
                val isCompactScreen = when (tabletMode) {
                    TabletMode.AUTO -> windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
                    TabletMode.ALWAYS -> false
                    TabletMode.LANDSCAPE -> LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
                    TabletMode.NEVER -> true
                }

                // 🚀 Lock 1: MAL
                val accessToken by viewModel.accessToken.collectAsStateWithLifecycle(App.accessToken)
                val isMalLoggedIn = !accessToken.isNullOrEmpty()

                // 🚀 Lock 2: Supabase
                val supabaseStatus by client.auth.sessionStatus.collectAsState(initial = SessionStatus.Initializing)
                val isSupabaseLoggedIn = supabaseStatus is SessionStatus.Authenticated

                val useListTabs by viewModel.useListTabs.collectAsStateWithLifecycle()
                val profilePicture by viewModel.profilePicture.collectAsStateWithLifecycle()
                val pinnedNavBar by viewModel.pinnedNavBar.collectAsStateWithLifecycle(false)

                MoeListTheme(
                    darkTheme = isDark,
                    useBlackColors = useBlackColors
                ) {
                    val backgroundColor = MaterialTheme.colorScheme.background
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = backgroundColor
                    ) {
                        MainView(
                            isCompactScreen = isCompactScreen,
                            isMalLoggedIn = isMalLoggedIn,       // Pass MAL Lock
                            isSupabaseLoggedIn = isSupabaseLoggedIn, // Pass Supabase Lock
                            useListTabs = useListTabs,
                            navController = navController,
                            navActionManager = navActionManager,
                            lastTabOpened = lastTabOpened,
                            saveLastTab = viewModel::saveLastTab,
                            pinnedNavBar = pinnedNavBar,
                            profilePicture = profilePicture,
                        )

                        DisposableEffect(isDark, navBackStackEntry) {
                            var statusBarStyle = SystemBarStyle.auto(
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT
                            ) { isDark }

                            if (isCompactScreen
                                && navBackStackEntry?.isBottomDestination() == true
                            ) {
                                statusBarStyle =
                                    if (isDark) SystemBarStyle.dark(backgroundColor.toArgb())
                                    else SystemBarStyle.light(
                                        backgroundColor.toArgb(),
                                        dark_scrim.toArgb()
                                    )
                            }
                            enableEdgeToEdge(
                                statusBarStyle = statusBarStyle,
                                navigationBarStyle = SystemBarStyle.auto(
                                    light_scrim.toArgb(),
                                    dark_scrim.toArgb(),
                                ) { isDark },
                            )
                            onDispose {}
                        }
                    }
                }

                LaunchedEffect(mediaId) {
                    if (mediaId != null && mediaTypeArg != null) {
                        val mediaType = MediaType.valueOf(mediaTypeArg)
                        navActionManager.toMediaDetails(mediaType, mediaId)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        client.handleDeeplinks(intent) // Also handle if app is already open in background
        checkLoginIntent(intent)
    }

    private fun checkLoginIntent(intent: Intent?) {
        if (intent?.data?.toString()?.startsWith(MOELIST_PAGELINK) == true) {
            intent.data?.let { viewModel.parseIntentData(it) }
        }
    }

    private fun findLastTabOpened(): Int {
        val startTab = runBlocking { viewModel.startTab.first() }
        var lastTabOpened =
            intent.action?.toBottomDestinationIndex() ?: startTab?.value?.toBottomDestinationIndex()

        if (lastTabOpened == null) {
            lastTabOpened = runBlocking { viewModel.lastTab.first() }
        } else { // opened from intent or start tab setting
            viewModel.saveLastTab(lastTabOpened)
        }
        return lastTabOpened
    }

    private fun findMediaIdAndTypeFromIntent(): Pair<Int, String>? {
        if (intent.action == "details") {
            val mediaId = intent.getIntExtra("media_id", 0)
            val mediaType = intent.getStringExtra("media_type")?.uppercase()
            if (mediaId != 0 && mediaType != null) return mediaId to mediaType
        } else if (intent.data != null) {
            val malSchemeIndex = intent.dataString?.indexOf("myanimelist.net")
            if (malSchemeIndex != null && malSchemeIndex != -1) {
                val isMainDetails = intent.data?.pathSegments?.any {
                    when (it) {
                        "character", "episode", "video", "reviews", "stacks", "news", "forum",
                        "clubs", "moreinfo" -> true
                        else -> false
                    }
                } == false
                if (!isMainDetails) {
                    intent.dataString?.let { openLink(it) }
                    return null
                }

                val linkSplit = intent.dataString?.substring(malSchemeIndex)?.split('/').orEmpty()
                val mediaType = linkSplit.getOrNull(1)?.uppercase()
                val mediaId = linkSplit.getOrNull(2)?.toIntOrNull()
                if (mediaType != null && mediaId != null) return mediaId to mediaType
            }
        }
        return null
    }
}

@Composable
fun MainView(
    isCompactScreen: Boolean,
    isMalLoggedIn: Boolean,
    isSupabaseLoggedIn: Boolean,
    useListTabs: Boolean,
    navController: NavHostController,
    navActionManager: NavActionManager,
    lastTabOpened: Int,
    saveLastTab: (Int) -> Unit,
    pinnedNavBar: Boolean,
    profilePicture: String?,
) {
    val density = LocalDensity.current

    var topBarHeightPx by remember { mutableFloatStateOf(0f) }
    val topBarOffsetY = remember { Animatable(0f) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isBottomDestination by remember {
        derivedStateOf { navBackStackEntry?.isBottomDestination() == true }
    }

    Scaffold(
        topBar = {
            if (isCompactScreen) {
                MainTopAppBar(
                    profilePicture = profilePicture,
                    isVisible = isBottomDestination,
                    navController = navController,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = topBarOffsetY.value
                        }
                )
            }
        },
        bottomBar = {
            if (isCompactScreen) {
                MainBottomNavBar(
                    navController = navController,
                    navBackStackEntry = navBackStackEntry,
                    isVisible = isBottomDestination || pinnedNavBar,
                    onItemSelected = saveLastTab,
                    topBarOffsetY = topBarOffsetY,
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        if (!isCompactScreen) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(padding)
            ) {
                MainNavigationRail(
                    navController = navController,
                    onItemSelected = saveLastTab,
                    modifier = Modifier.safeDrawingPadding(),
                )
                MainNavigation(
                    navController = navController,
                    navActionManager = navActionManager,
                    lastTabOpened = lastTabOpened,
                    isMalLoggedIn = isMalLoggedIn,           // Pass MAL Lock
                    isSupabaseLoggedIn = isSupabaseLoggedIn, // Pass Supabase Lock
                    isCompactScreen = false,
                    useListTabs = useListTabs,
                    modifier = Modifier,
                    padding = PaddingValues(),
                    topBarHeightPx = topBarHeightPx,
                    topBarOffsetY = topBarOffsetY,
                )
            }
        } else {
            LaunchedEffect(padding) {
                topBarHeightPx = density.run { padding.calculateTopPadding().toPx() }
            }
            MainNavigation(
                navController = navController,
                navActionManager = navActionManager,
                lastTabOpened = lastTabOpened,
                isMalLoggedIn = isMalLoggedIn,           // Pass MAL Lock
                isSupabaseLoggedIn = isSupabaseLoggedIn, // Pass Supabase Lock
                isCompactScreen = true,
                useListTabs = useListTabs,
                modifier = Modifier.padding(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = if (pinnedNavBar) padding.calculateBottomPadding() else 0.dp,
                ),
                padding = PaddingValues(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = if (pinnedNavBar) 0.dp else padding.calculateBottomPadding(),
                ),
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
        }
    }
}

@Preview
@Composable
fun MainPreview() {
    MoeListTheme {
        Surface {
            MainView(
                isCompactScreen = true,
                isMalLoggedIn = false,
                isSupabaseLoggedIn = false,
                useListTabs = false,
                navController = rememberNavController(),
                navActionManager = rememberNavActionManager(),
                lastTabOpened = 0,
                saveLastTab = {},
                pinnedNavBar = false,
                profilePicture = null,
            )
        }
    }
}