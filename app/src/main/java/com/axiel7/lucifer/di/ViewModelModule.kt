package com.axiel7.lucifer.di

import com.axiel7.lucifer.ui.calendar.CalendarViewModel
import com.axiel7.lucifer.ui.custommedia.CustomMediaViewModel
import com.axiel7.lucifer.ui.details.MediaDetailsViewModel
import com.axiel7.lucifer.ui.editmedia.EditMediaViewModel
import com.axiel7.lucifer.ui.explore.ExploreCategoryViewModel
import com.axiel7.lucifer.ui.explore.ExploreViewModel
import com.axiel7.lucifer.ui.home.HomeViewModel
import com.axiel7.lucifer.ui.main.MainViewModel
import com.axiel7.lucifer.ui.more.MoreViewModel
import com.axiel7.lucifer.ui.more.notifications.NotificationsViewModel
import com.axiel7.lucifer.ui.more.settings.SettingsViewModel
import com.axiel7.lucifer.ui.more.settings.list.ListStyleSettingsViewModel
import com.axiel7.lucifer.ui.profile.ProfileViewModel
import com.axiel7.lucifer.ui.ranking.MediaRankingViewModel
import com.axiel7.lucifer.ui.recommendations.RecommendationsViewModel
import com.axiel7.lucifer.ui.search.SearchViewModel
import com.axiel7.lucifer.ui.season.SeasonChartViewModel
import com.axiel7.lucifer.ui.userlist.UserMediaListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::MediaDetailsViewModel)
    viewModel { params ->
        EditMediaViewModel(
            mediaType = params.get(),
            animeRepository = get(),
            mangaRepository = get()
        )
    }
    viewModelOf(::HomeViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::ListStyleSettingsViewModel)
    viewModel {
        NotificationsViewModel(
            dataStore = get(named(NOTIFICATIONS_DATA_STORE)),
            notificationWorkerManager = get()
        )
    }
    viewModelOf(::ProfileViewModel)
    viewModel { params ->
        MediaRankingViewModel(
            rankingType = params.get(),
            savedStateHandle = get(),
            defaultPreferencesRepository = get(),
            animeRepository = get(),
            mangaRepository = get()
        )
    }
    viewModelOf(::RecommendationsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SeasonChartViewModel)
    viewModel { params ->
        UserMediaListViewModel(
            mediaType = params.get(),
            initialListStatus = params.getOrNull(),
            animeRepository = get(),
            mangaRepository = get(),
            defaultPreferencesRepository = get(),
            supabaseRepository = get()
        )
    }
    viewModelOf(::ExploreCategoryViewModel)
    viewModelOf(::ExploreViewModel)
    viewModelOf(::MoreViewModel)
    viewModelOf(::CustomMediaViewModel)
}