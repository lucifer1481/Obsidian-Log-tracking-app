package com.axiel7.lucifer.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axiel7.lucifer.data.model.anime.AnimeDetails
import com.axiel7.lucifer.data.model.anime.MyAnimeListStatus
import com.axiel7.lucifer.data.model.anime.Recommendations
import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.model.manga.MangaDetails
import com.axiel7.lucifer.data.model.manga.MyMangaListStatus
import com.axiel7.lucifer.data.model.media.BaseMediaNode
import com.axiel7.lucifer.data.model.media.BaseMyListStatus
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.data.model.media.WeekDay
import com.axiel7.lucifer.data.repository.AnimeRepository
import com.axiel7.lucifer.data.repository.DefaultPreferencesRepository
import com.axiel7.lucifer.data.repository.ExternalMediaRepository
import com.axiel7.lucifer.data.repository.MangaRepository
import com.axiel7.lucifer.data.repository.SupabaseRepository
import com.axiel7.lucifer.ui.base.navigation.Route
import com.axiel7.lucifer.ui.base.viewmodel.BaseViewModel
import com.axiel7.lucifer.worker.NotificationWorkerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.typeOf

@Suppress("UNCHECKED_CAST")
class MediaDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val externalMediaRepository: ExternalMediaRepository,
    private val supabaseRepository: SupabaseRepository,
    private val notificationWorkerManager: NotificationWorkerManager,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<MediaDetailsUiState>(), MediaDetailsEvent {

    private val args = savedStateHandle.toRoute<Route.MediaDetails>(typeMap = mapOf(typeOf<MediaType>() to MediaType.navType))
    private val mediaType = args.mediaType

    // 🚀 FIXED: Exposing mediaId so the view knows exactly what to save
    val currentMediaId = args.mediaId

    // 🚀 NEW: Caches your saved item so the bottom sheet knows your current status/score
    var savedCloudMedia: CloudMedia? = null

    override val mutableUiState = MutableStateFlow(MediaDetailsUiState(mediaType = mediaType))

    override fun onChangedMyListStatus(value: BaseMyListStatus?, removed: Boolean) {
        mutableUiState.update {
            when (it.mediaDetails) {
                is AnimeDetails -> it.copy(mediaDetails = it.mediaDetails.copy(myListStatus = (value as? MyAnimeListStatus).takeIf { !removed }))
                is MangaDetails -> it.copy(mediaDetails = it.mediaDetails.copy(myListStatus = (value as? MyMangaListStatus).takeIf { !removed }))
                else -> it
            }
        }
    }

    // Keep this empty so your Event interface doesn't crash, we'll use the new one below
    override fun saveExternalToSupabase() {}

    // 🚀 FIXED: New save function that actually accepts the edited data from the Bottom Sheet!
    fun saveExternalMedia(cloudMedia: CloudMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = supabaseRepository.upsertMedia(cloudMedia)
            if (success) {
                savedCloudMedia = cloudMedia // Update the local cache with your edits
            }
            mutableUiState.update {
                it.copy(
                    isSavedInSupabase = success,
                    message = if (success) "Saved to your Library!" else "Failed to save."
                )
            }
        }
    }

    override fun getCharacters() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.update { it.copy(isLoadingCharacters = true) }
            val result = animeRepository.getAnimeCharacters(animeId = currentMediaId, limit = 40, offset = null, page = null)
            if (result.wasError) mutableUiState.update { it.copy(isLoadingCharacters = false, message = result.message ?: "Error loading characters") }
            else mutableUiState.update { it.copy(characters = result.data.orEmpty().sortedBy { char -> char.role }, isLoadingCharacters = false) }
        }
    }

    override fun scheduleAiringAnimeNotification(title: String, animeId: Int, weekDay: WeekDay, jpHour: LocalTime) {
        viewModelScope.launch { notificationWorkerManager.scheduleAiringAnimeNotification(title, animeId, weekDay, jpHour) }
    }

    override fun scheduleAnimeStartNotification(title: String, animeId: Int, startDate: LocalDate) {
        viewModelScope.launch { notificationWorkerManager.scheduleAnimeStartNotification(title, animeId, startDate) }
    }

    override fun removeAiringAnimeNotification(animeId: Int) {
        viewModelScope.launch { notificationWorkerManager.removeAiringAnimeNotification(animeId) }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)

            // 🚀 FIXED: Fetch the actual saved item from Supabase to pre-fill the Edit Sheet
            if (mediaType != MediaType.ANIME && mediaType != MediaType.MANGA) {
                val allSaved = supabaseRepository.getSavedMedia(mediaType.name)
                savedCloudMedia = allSaved.find { it.apiId == currentMediaId.toLong() }
                mutableUiState.update { it.copy(isSavedInSupabase = savedCloudMedia != null) }
            }

            if (mediaType == MediaType.ANIME || mediaType == MediaType.MANGA) {
                val mediaDetails = if (mediaType == MediaType.ANIME) animeRepository.getAnimeDetails(currentMediaId) else mangaRepository.getMangaDetails(currentMediaId)
                if (mediaDetails == null) showMessage("Unable to reach server")
                else if (mediaDetails.error != null) showMessage(mediaDetails.error)
                else {
                    val recommendations = (mediaDetails.recommendations as? List<Recommendations<BaseMediaNode>>).orEmpty()
                    val picturesUrls = listOf(mediaDetails.mainPicture?.large.orEmpty()).plus(mediaDetails.pictures?.map { it.large ?: it.medium.orEmpty() }.orEmpty())
                    mutableUiState.update { it.copy(mediaDetails = mediaDetails, relatedAnime = mediaDetails.relatedAnime.orEmpty(), relatedManga = mediaDetails.relatedManga.orEmpty(), recommendations = recommendations, picturesUrls = picturesUrls, isLoading = false) }
                    if (mediaType == MediaType.ANIME && defaultPreferencesRepository.loadCharacters.first()) getCharacters()
                }
            } else {
                when (mediaType) {
                    MediaType.MOVIES -> {
                        val movie = externalMediaRepository.getMovieDetails(currentMediaId)
                        mutableUiState.update {
                            it.copy(
                                externalTitle = movie?.title,
                                externalPoster = movie?.posterPath?.let { path -> "https://image.tmdb.org/t/p/w500$path" },
                                externalSynopsis = movie?.overview,
                                externalScore = movie?.voteAverage,
                                externalReleaseDate = movie?.releaseDate,
                                externalGenres = movie?.genres?.map { genre -> genre.name }.orEmpty(),
                                externalRuntime = movie?.runtime?.let { mins -> "$mins min" },
                                externalStatus = movie?.status,
                                externalStudios = movie?.productionCompanies?.joinToString(", ") { comp -> comp.name },
                                picturesUrls = movie?.posterPath?.let { path -> listOf("https://image.tmdb.org/t/p/original$path") }.orEmpty(),
                                isLoading = false
                            )
                        }
                    }
                    MediaType.SERIES -> {
                        val series = externalMediaRepository.getSeriesDetails(currentMediaId)
                        mutableUiState.update {
                            it.copy(
                                externalTitle = series?.name,
                                externalPoster = series?.posterPath?.let { path -> "https://image.tmdb.org/t/p/w500$path" },
                                externalSynopsis = series?.overview,
                                externalScore = series?.voteAverage,
                                externalReleaseDate = series?.firstAirDate,
                                externalGenres = series?.genres?.map { genre -> genre.name }.orEmpty(),
                                externalRuntime = series?.episodeRunTime?.firstOrNull()?.let { mins -> "$mins min/ep" },
                                externalStatus = series?.status,
                                externalStudios = series?.productionCompanies?.joinToString(", ") { comp -> comp.name },
                                picturesUrls = series?.posterPath?.let { path -> listOf("https://image.tmdb.org/t/p/original$path") }.orEmpty(),
                                isLoading = false
                            )
                        }
                    }
                    MediaType.GAMES -> {
                        val game = externalMediaRepository.getGameDetails(currentMediaId)
                        mutableUiState.update {
                            it.copy(
                                externalTitle = game?.name,
                                externalPoster = game?.backgroundImage,
                                externalSynopsis = game?.description_raw,
                                externalScore = game?.rating?.times(2),
                                externalReleaseDate = game?.released,
                                externalGenres = game?.genres?.map { genre -> genre.name }.orEmpty(),
                                externalRuntime = game?.playtime?.let { hrs -> if (hrs > 0) "~$hrs hours" else null },
                                externalStatus = if (game?.released != null) "Released" else "In Development",
                                externalStudios = game?.developers?.joinToString(", ") { dev -> dev.name },
                                picturesUrls = listOfNotNull(game?.backgroundImage),
                                isLoading = false
                            )
                        }
                    }
                    else -> mutableUiState.update { it.copy(isLoading = false) }
                }
            }
        }

        defaultPreferencesRepository.hideScores.onEach { value -> mutableUiState.update { it.copy(hideScore = value) } }.launchIn(viewModelScope)
        notificationWorkerManager.getNotification(currentMediaId).onEach { notification -> mutableUiState.update { it.copy(notification = notification) } }.launchIn(viewModelScope)
        notificationWorkerManager.getStartNotification(currentMediaId).onEach { startNotification -> mutableUiState.update { it.copy(startNotification = startNotification) } }.launchIn(viewModelScope)
    }
}