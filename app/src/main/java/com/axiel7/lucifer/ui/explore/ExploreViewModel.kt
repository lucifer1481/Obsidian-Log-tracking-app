package com.axiel7.lucifer.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.lucifer.data.model.custom.RawgItem
import com.axiel7.lucifer.data.model.custom.TmdbItem
import com.axiel7.lucifer.data.repository.ExternalMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val isLoading: Boolean = true,
    val trendingMovies: List<TmdbItem> = emptyList(),
    val popularSeries: List<TmdbItem> = emptyList(),
    val trendingGames: List<RawgItem> = emptyList()
)

class ExploreViewModel(
    private val externalMediaRepository: ExternalMediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadExploreData()
    }

    private fun loadExploreData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            // Fetch everything at the same time
            val moviesRes = externalMediaRepository.getTrendingMovies()
            val seriesRes = externalMediaRepository.getPopularSeries()
            val gamesRes = externalMediaRepository.getTrendingGames()

            _uiState.update { state ->
                state.copy(
                    trendingMovies = moviesRes?.results ?: emptyList(),
                    popularSeries = seriesRes?.results ?: emptyList(),
                    trendingGames = gamesRes?.results ?: emptyList(),
                    isLoading = false
                )
            }
        }
    }
}