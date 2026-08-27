package com.axiel7.lucifer.ui.home

import androidx.lifecycle.viewModelScope
import com.axiel7.lucifer.data.model.anime.AnimeRanking
import com.axiel7.lucifer.data.model.media.MediaSort
import com.axiel7.lucifer.data.model.media.MediaStatus
import com.axiel7.lucifer.data.model.media.RankingType
import com.axiel7.lucifer.data.repository.AnimeRepository
import com.axiel7.lucifer.data.repository.DefaultPreferencesRepository
import com.axiel7.lucifer.ui.base.viewmodel.BaseViewModel
import com.axiel7.lucifer.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val animeRepository: AnimeRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<HomeUiState>(), HomeEvent {

    override val mutableUiState = MutableStateFlow(HomeUiState())

    override fun initRequestChain(isLoggedIn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // 🚀 FIX: Add a brief delay on cold start to let the MAL token refresh in the background
            if (mutableUiState.value.todayAnimes.isEmpty() && mutableUiState.value.seasonAnimes.isEmpty()) {
                delay(1000)
            }

            mutableUiState.update { it.copy(isLoading = true) }
            mutableUiState.value.run {
                if (todayAnimes.isEmpty()) getTodayAiringAnimes()
                if (seasonAnimes.isEmpty()) getSeasonAnimes()
                if (isLoggedIn && recommendedAnimes.isEmpty()) getRecommendedAnimes()
            }
            mutableUiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun getTodayAiringAnimes(retryCount: Int = 0) {
        val result = animeRepository.getAnimeRanking(
            rankingType = RankingType.AIRING,
            limit = 100,
            fields = AnimeRepository.TODAY_FIELDS
        )
        if (result.data != null) {
            val tempList = mutableListOf<AnimeRanking>()
            for (anime in result.data) {
                if (anime.node.broadcast != null
                    && !tempList.contains(anime)
                    && anime.node.broadcast.dayOfTheWeek == SeasonCalendar.currentJapanWeekday
                    && anime.node.status == MediaStatus.AIRING
                ) {
                    tempList.add(anime)
                }
            }
            tempList.sortByDescending { it.node.broadcast?.startTime }
            mutableUiState.update { it.copy(todayAnimes = tempList) }
        } else {
            // 🚀 FIX: Smart retry if the token was caught mid-refresh
            val isTokenError = result.error?.contains("invalid_token") == true || result.message?.contains("invalid_token") == true
            if (isTokenError && retryCount < 2) {
                delay(1500)
                getTodayAiringAnimes(retryCount + 1)
            } else {
                showMessage(result.message ?: result.error)
            }
        }
    }

    private suspend fun getSeasonAnimes(retryCount: Int = 0) {
        val currentStartSeason = SeasonCalendar.currentStartSeason
        val result = animeRepository.getSeasonalAnimes(
            sort = MediaSort.SCORE,
            startSeason = currentStartSeason,
            limit = 25,
            fields = "alternative_titles{en,ja},mean,my_list_status{status}",
        )
        if (result.data != null) {
            mutableUiState.update { it.copy(seasonAnimes = result.data) }
        } else {
            // 🚀 FIX: Smart retry
            val isTokenError = result.error?.contains("invalid_token") == true || result.message?.contains("invalid_token") == true
            if (isTokenError && retryCount < 2) {
                delay(1500)
                getSeasonAnimes(retryCount + 1)
            } else {
                showMessage(result.message ?: result.error)
            }
        }
    }

    private suspend fun getRecommendedAnimes(retryCount: Int = 0) {
        val result = animeRepository.getRecommendedAnimes(
            limit = 25
        )
        if (result.data != null) {
            mutableUiState.update { it.copy(recommendedAnimes = result.data) }
        } else {
            // 🚀 FIX: Smart retry
            val isTokenError = result.error?.contains("invalid_token") == true || result.message?.contains("invalid_token") == true
            if (isTokenError && retryCount < 2) {
                delay(1500)
                getRecommendedAnimes(retryCount + 1)
            } else {
                showMessage(result.message ?: result.error)
            }
        }
    }

    init {
        defaultPreferencesRepository.hideScores
            .onEach { value ->
                mutableUiState.update { it.copy(hideScore = value) }
            }
            .launchIn(viewModelScope)
    }
}