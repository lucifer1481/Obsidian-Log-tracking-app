package com.axiel7.lucifer.ui.home

import androidx.compose.runtime.Immutable
import com.axiel7.lucifer.data.model.anime.AnimeList
import com.axiel7.lucifer.data.model.anime.AnimeRanking
import com.axiel7.lucifer.data.model.anime.AnimeSeasonal
import com.axiel7.lucifer.ui.base.state.UiState

@Immutable
data class HomeUiState(
    val todayAnimes: List<AnimeRanking> = emptyList(),
    val seasonAnimes: List<AnimeSeasonal> = emptyList(),
    val recommendedAnimes: List<AnimeList> = emptyList(),
    val hideScore: Boolean = false,
    override val isLoading: Boolean = true,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
