package com.axiel7.lucifer.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.data.repository.ExternalMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreGridItem(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: MediaType
)

data class ExploreCategoryUiState(
    val categoryName: String = "",
    val items: List<ExploreGridItem> = emptyList(),
    val isLoading: Boolean = false,
    val canLoadMore: Boolean = true
)

class ExploreCategoryViewModel(
    private val externalMediaRepository: ExternalMediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreCategoryUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 1

    fun loadCategory(category: String) {
        if (_uiState.value.items.isNotEmpty()) return
        _uiState.update { it.copy(categoryName = category, isLoading = true) }
        fetchPage()
    }

    fun loadMore() {
        if (!_uiState.value.canLoadMore || _uiState.value.isLoading) return
        currentPage++
        fetchPage()
    }

    private fun fetchPage() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val newItems = mutableListOf<ExploreGridItem>()
            var canLoadMore = true

            when (_uiState.value.categoryName) {
                "MOVIES" -> {
                    val res = externalMediaRepository.getTrendingMovies(currentPage)
                    res?.results?.forEach {
                        // 🚀 FIXED: Added .toInt() to it.id
                        newItems.add(ExploreGridItem(it.id.toInt(), it.title ?: "", it.posterPath?.let { p -> "https://image.tmdb.org/t/p/w500$p" }, MediaType.MOVIES))
                    }
                    if (res?.results.isNullOrEmpty()) canLoadMore = false
                }
                "SERIES" -> {
                    val res = externalMediaRepository.getPopularSeries(currentPage)
                    res?.results?.forEach {
                        // 🚀 FIXED: Added .toInt() to it.id
                        newItems.add(ExploreGridItem(it.id.toInt(), it.name ?: "", it.posterPath?.let { p -> "https://image.tmdb.org/t/p/w500$p" }, MediaType.SERIES))
                    }
                    if (res?.results.isNullOrEmpty()) canLoadMore = false
                }
                "GAMES" -> {
                    val res = externalMediaRepository.getTrendingGames(currentPage)
                    res?.results?.forEach {
                        // 🚀 FIXED: Added .toInt() to it.id
                        newItems.add(ExploreGridItem(it.id.toInt(), it.name, it.backgroundImage, MediaType.GAMES))
                    }
                    if (res?.results.isNullOrEmpty()) canLoadMore = false
                    _uiState.update { state ->
                        state.copy(
                            items = (state.items + newItems).distinctBy { it.id },
                            isLoading = false,
                            canLoadMore = canLoadMore
                        )
                    }
                }

            }

            _uiState.update { state ->
                state.copy(
                    items = state.items + newItems,
                    isLoading = false,
                    canLoadMore = canLoadMore
                )
            }
        }
    }
}