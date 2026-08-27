package com.axiel7.moelist.ui.details

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.Recommendations
import com.axiel7.moelist.data.model.anime.RelatedAnime
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.RelatedManga
import com.axiel7.moelist.data.model.media.BaseMediaDetails
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.Character
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.state.UiState

@Immutable
data class MediaDetailsUiState(
    val mediaType: MediaType = MediaType.ANIME,
    val mediaDetails: BaseMediaDetails? = null,
    val hideScore: Boolean = false,
    val relatedAnime: List<RelatedAnime> = emptyList(),
    val relatedManga: List<RelatedManga> = emptyList(),
    val recommendations: List<Recommendations<BaseMediaNode>> = emptyList(),
    val picturesUrls: List<String> = emptyList(),
    val characters: List<Character> = emptyList(),
    val isLoadingCharacters: Boolean = false,
    val notification: String? = null,
    val startNotification: String? = null,

    // External Media State (Movies, Series, Games)
    val externalTitle: String? = null,
    val externalPoster: String? = null,
    val externalSynopsis: String? = null,
    val externalScore: Double? = null,
    val externalReleaseDate: String? = null,
    // 🚀 NEW: Rich Details State
    val externalGenres: List<String> = emptyList(),
    val externalRuntime: String? = null,
    val externalStatus: String? = null,
    val externalStudios: String? = null,
    val isSavedInSupabase: Boolean = false,

    override val isLoading: Boolean = true,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)

    val isExternal: Boolean get() = mediaType == MediaType.MOVIES || mediaType == MediaType.SERIES || mediaType == MediaType.GAMES

    val mediaInfo: BaseMediaNode?
        get() = (mediaDetails as? AnimeDetails)?.toAnimeNode()
            ?: (mediaDetails as? MangaDetails)?.toMangaNode()

    val myListStatus: BaseMyListStatus? get() = mediaDetails?.myListStatus

    val isNewEntry get() = if (isExternal) !isSavedInSupabase else mediaDetails?.myListStatus == null

    val isAnime get() = mediaDetails is AnimeDetails
    val studiosJoined = (mediaDetails as? AnimeDetails)?.studios?.joinToString { it.name }
    val serializationJoined = (mediaDetails as? MangaDetails)?.serialization?.joinToString { it.node.name }
}