package com.axiel7.lucifer.ui.userlist

import androidx.lifecycle.viewModelScope
import com.axiel7.lucifer.data.model.anime.MyAnimeListStatus
import com.axiel7.lucifer.data.model.anime.UserAnimeList
import com.axiel7.lucifer.data.model.manga.MyMangaListStatus
import com.axiel7.lucifer.data.model.manga.UserMangaList
import com.axiel7.lucifer.data.model.media.BaseMediaNode
import com.axiel7.lucifer.data.model.media.BaseMyListStatus
import com.axiel7.lucifer.data.model.media.BaseUserMediaList
import com.axiel7.lucifer.data.model.media.ListStatus
import com.axiel7.lucifer.data.model.media.ListType
import com.axiel7.lucifer.data.model.media.MediaSort
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.data.repository.AnimeRepository
import com.axiel7.lucifer.data.repository.DefaultPreferencesRepository
import com.axiel7.lucifer.data.repository.MangaRepository
import com.axiel7.lucifer.data.repository.SupabaseRepository
import com.axiel7.lucifer.ui.base.viewmodel.BaseViewModel
import com.axiel7.lucifer.utils.NumExtensions.isGreaterThanZero
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class UserMediaListViewModel(
    mediaType: MediaType,
    initialListStatus: ListStatus? = null,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
    private val supabaseRepository: SupabaseRepository,
) : BaseViewModel<UserMediaListUiState>(), UserMediaListEvent {

    private val defaultListStatus = when (mediaType) {
        MediaType.ANIME -> ListStatus.WATCHING
        MediaType.MANGA -> ListStatus.READING
        else -> ListStatus.WATCHING
    }

    override val mutableUiState = MutableStateFlow(
        UserMediaListUiState(
            mediaType = mediaType,
            listStatus = initialListStatus
        )
    )

    // 🚀 FIXED: Removed the old disconnected cloudMediaList variable to prevent data getting lost!

    override fun onChangeStatus(value: ListStatus) {
        viewModelScope.launch {
            mutableUiState.update {
                when (it.mediaType) {
                    MediaType.ANIME -> defaultPreferencesRepository.setAnimeListStatus(value)
                    MediaType.MANGA -> defaultPreferencesRepository.setMangaListStatus(value)
                    else -> {}
                }
                it.mediaList.clear()
                it.copy(
                    listStatus = value,
                    nextPage = null,
                    loadMore = true
                )
            }
        }
    }

    override fun onChangeSort(value: MediaSort) {
        viewModelScope.launch {
            mutableUiState.update {
                when (it.mediaType) {
                    MediaType.ANIME -> defaultPreferencesRepository.setAnimeListSort(value)
                    MediaType.MANGA -> defaultPreferencesRepository.setMangaListSort(value)
                    else -> {}
                }
                it.mediaList.clear()
                it.copy(
                    listSort = value,
                    nextPage = null,
                    loadMore = true
                )
            }
        }
    }

    override fun onChangeItemMyListStatus(value: BaseMyListStatus?, removed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.value.run {
                if (selectedItem != null) {
                    val foundIndex = mediaList.indexOfFirst { it.node.id == selectedItem.node.id }
                    if (foundIndex != -1) {
                        if (removed) {
                            mediaList.removeAt(foundIndex)
                        } else if (value != null) {
                            val statusChanged =
                                value.status != mediaList[foundIndex].listStatus?.status
                            when {
                                statusChanged -> mediaList.removeAt(foundIndex)
                                mediaType == MediaType.ANIME -> {
                                    mediaList[foundIndex] = (mediaList[foundIndex] as UserAnimeList)
                                        .copy(listStatus = value as MyAnimeListStatus)
                                }
                                mediaType == MediaType.MANGA -> {
                                    mediaList[foundIndex] = (mediaList[foundIndex] as UserMangaList)
                                        .copy(listStatus = value as MyMangaListStatus)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun refreshList() {
        mutableUiState.update { it.copy(nextPage = null, loadMore = true) }
        if (mutableUiState.value.isExternal) {
            loadMediaForCurrentType()
        }
    }

    override fun loadMore() {
        mutableUiState.value.run {
            if (canLoadMore && !isLoadingMore) {
                mutableUiState.update { it.copy(loadMore = true) }
            }
        }
    }

    override fun onUpdateProgress(item: BaseUserMediaList<out BaseMediaNode>) {
        mutableUiState.update { it.copy(selectedItem = item) }
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val nowDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            var newStatus: ListStatus? = null
            val result = when (item) {
                is UserAnimeList -> {
                    val newProgress = (item.listStatus?.progress ?: 0) + 1
                    val maxProgress = item.node.numEpisodes.takeIf { it != 0 }
                    val isCompleted = maxProgress != null && newProgress >= maxProgress
                    val isPlanning = item.listStatus?.status == ListStatus.PLAN_TO_WATCH
                    newStatus = when {
                        isCompleted -> ListStatus.COMPLETED
                        isPlanning -> ListStatus.WATCHING
                        else -> null
                    }
                    animeRepository.updateAnimeEntry(
                        animeId = item.node.id,
                        watchedEpisodes = newProgress,
                        status = newStatus,
                        startDate = nowDate.takeIf {
                            isPlanning || !item.listStatus?.progress.isGreaterThanZero()
                        },
                        endDate = nowDate.takeIf { isCompleted }
                    )
                }

                is UserMangaList -> {
                    val isVolumeProgress = item.listStatus?.isUsingVolumeProgress() == true
                    val newProgress = if (isVolumeProgress) {
                        (item.listStatus?.numVolumesRead ?: 0) + 1
                    } else {
                        (item.listStatus?.progress ?: 0) + 1
                    }
                    val maxProgress =
                        (if (isVolumeProgress) item.node.numVolumes else item.node.numChapters)
                            .takeIf { it != 0 }
                    val isCompleted = maxProgress != null && newProgress >= maxProgress
                    val isPlanning = item.listStatus?.status == ListStatus.PLAN_TO_READ
                    newStatus = when {
                        isCompleted -> ListStatus.COMPLETED
                        isPlanning -> ListStatus.READING
                        else -> null
                    }
                    mangaRepository.updateMangaEntry(
                        mangaId = item.node.id,
                        chaptersRead = newProgress.takeIf { !isVolumeProgress },
                        volumesRead = newProgress.takeIf { isVolumeProgress },
                        status = newStatus,
                        startDate = nowDate.takeIf {
                            isPlanning || item.listStatus?.progress.isGreaterThanZero()
                        },
                        endDate = nowDate.takeIf { isCompleted }
                    )
                }

                else -> null
            }

            if (result != null) {
                mutableUiState.value.run {
                    val foundIndex = mediaList.indexOfFirst { it.node.id == item.node.id }
                    if (foundIndex != -1) {
                        if (newStatus != null) {
                            if (newStatus == ListStatus.COMPLETED && result.score == 0) {
                                toggleSetScoreDialog(true)
                            }
                            mediaList.removeAt(foundIndex)
                        } else {
                            if (mediaType == MediaType.ANIME) {
                                mediaList[foundIndex] = (mediaList[foundIndex] as UserAnimeList)
                                    .copy(listStatus = result as MyAnimeListStatus)
                            } else if (mediaType == MediaType.MANGA) {
                                mediaList[foundIndex] = (mediaList[foundIndex] as UserMangaList)
                                    .copy(listStatus = result as MyMangaListStatus)
                            }
                        }
                    }
                }
            }
            setLoading(false)
        }
    }

    override fun onItemSelected(item: BaseUserMediaList<*>) {
        mutableUiState.update { it.copy(selectedItem = item) }
    }

    override fun setScore(score: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            mutableUiState.value.run {
                if (selectedItem == null) return@launch
                if (mediaType == MediaType.ANIME) {
                    animeRepository.updateAnimeEntry(
                        animeId = selectedItem.node.id,
                        score = score
                    )
                } else {
                    mangaRepository.updateMangaEntry(
                        mangaId = selectedItem.node.id,
                        score = score
                    )
                }
            }
            mutableUiState.update {
                it.copy(openSetScoreDialog = false, isLoading = false)
            }
        }
    }

    override fun toggleSortDialog(open: Boolean) {
        mutableUiState.update { it.copy(openSortDialog = open) }
    }

    override fun toggleSetScoreDialog(open: Boolean) {
        mutableUiState.update { it.copy(openSetScoreDialog = open) }
    }

    override fun getRandomIdOfList() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.run {
                emit(value.copy(isLoadingRandom = true))

                val result = when (value.mediaType) {
                    MediaType.ANIME -> {
                        animeRepository.getAnimeIdsOfUserList(
                            status = value.listStatus ?: defaultListStatus
                        )
                    }
                    MediaType.MANGA -> {
                        mangaRepository.getMangaIdsOfUserList(
                            status = value.listStatus ?: defaultListStatus
                        )
                    }
                    else -> com.axiel7.lucifer.data.model.Response(data = emptyList())
                }

                if (!result.data.isNullOrEmpty()) {
                    emit(
                        value.copy(
                            randomId = result.data.random(),
                            isLoadingRandom = false,
                        )
                    )
                } else {
                    emit(value.copy(isLoadingRandom = false))
                }
            }
        }
    }

    // Bulletproof Exact Status Matcher with Ghost-Catching
    private fun isStatusMatching(uiStatus: String?, dbStatus: String?): Boolean {
        if (uiStatus == null) return true

        val actualDbStatus = if (dbStatus.isNullOrBlank()) "PLAN_TO_WATCH" else dbStatus
        val safeDbStatus = actualDbStatus.uppercase()

        return when (uiStatus.uppercase()) {
            "WATCHING", "READING", "PLAYING" -> safeDbStatus in listOf("WATCHING", "READING", "PLAYING")
            "PLAN_TO_WATCH", "PLAN_TO_READ", "PLAN_TO_PLAY" -> safeDbStatus in listOf("PLAN_TO_WATCH", "PLAN_TO_READ", "PLAN_TO_PLAY")
            "ON_HOLD" -> safeDbStatus == "ON_HOLD"
            "COMPLETED" -> safeDbStatus == "COMPLETED"
            "DROPPED" -> safeDbStatus == "DROPPED"
            else -> safeDbStatus == uiStatus.uppercase()
        }
    }

    fun loadMediaForCurrentType() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                val currentType = mutableUiState.value.mediaType.name
                val cloudItems = supabaseRepository.getSavedMedia(currentType)

                val currentStatusName = mutableUiState.value.listStatus?.name
                val filteredItems = cloudItems.filter { item ->
                    isStatusMatching(currentStatusName, item.status)
                }

                // 🚀 FIXED: Save the filtered items directly into the UI state!
                mutableUiState.update {
                    it.copy(
                        cloudMediaList = filteredItems,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mutableUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    override fun onRandomIdOpen() {
        mutableUiState.update { it.copy(randomId = null) }
    }

    init {
        if (initialListStatus == null) {
            val listStatusFlow = when (mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.animeListStatus
                MediaType.MANGA -> defaultPreferencesRepository.mangaListStatus
                else -> defaultPreferencesRepository.animeListStatus
            }
            viewModelScope.launch {
                mutableUiState.update {
                    it.copy(listStatus = listStatusFlow.first())
                }
            }
        }

        val listSortFlow = when (mediaType) {
            MediaType.ANIME -> defaultPreferencesRepository.animeListSort
            MediaType.MANGA -> defaultPreferencesRepository.mangaListSort
            else -> defaultPreferencesRepository.animeListSort
        }
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(listSort = listSortFlow.first())
            }
        }

        combine(
            defaultPreferencesRepository.useGeneralListStyle,
            defaultPreferencesRepository.generalListStyle
        ) { useGeneral, generalStyle ->
            if (useGeneral) {
                mutableUiState.update { it.copy(listStyle = generalStyle) }
            } else {
                mutableUiState
                    .filter { it.listStatus != null }
                    .flatMapLatest {
                        ListType(it.listStatus!!, it.mediaType)
                            .stylePreference(defaultPreferencesRepository)
                    }.collect { listStyle ->
                        mutableUiState.update { it.copy(listStyle = listStyle) }
                    }
            }
        }.launchIn(viewModelScope)

        defaultPreferencesRepository.gridItemsPerRow
            .onEach { value ->
                mutableUiState.update { it.copy(itemsPerRow = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.randomListEntryEnabled
            .onEach { value ->
                mutableUiState.update { it.copy(showRandomButton = value) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState
                .distinctUntilChanged { old, new ->
                    old.mediaType == new.mediaType
                            && old.loadMore == new.loadMore
                            && old.listStatus == new.listStatus
                            && old.listSort == new.listSort
                }
                .filter { it.loadMore || it.mediaType != mediaType || it.listStatus != null }
                .collectLatest { uiState ->
                    if ((uiState.mediaType == MediaType.ANIME || uiState.mediaType == MediaType.MANGA) &&
                        (uiState.listStatus == null || uiState.listSort == null)
                    ) {
                        return@collectLatest
                    }

                    mutableUiState.update {
                        it.copy(
                            isLoadingMore = true,
                            isLoading = uiState.nextPage == null
                        )
                    }

                    if (uiState.mediaType == MediaType.ANIME || uiState.mediaType == MediaType.MANGA) {
                        val result = when (uiState.mediaType) {
                            MediaType.ANIME -> {
                                animeRepository.getUserAnimeList(
                                    status = uiState.listStatus!!,
                                    sort = uiState.listSort!!,
                                    page = uiState.nextPage
                                )
                            }
                            MediaType.MANGA -> {
                                mangaRepository.getUserMangaList(
                                    status = uiState.listStatus!!,
                                    sort = uiState.listSort!!,
                                    page = uiState.nextPage
                                )
                            }
                            else -> com.axiel7.lucifer.data.model.Response(error = "Not implemented")
                        }

                        if (result.data != null) {
                            if (uiState.nextPage == null) uiState.mediaList.clear()
                            uiState.mediaList.addAll(result.data)
                            mutableUiState.update {
                                it.copy(
                                    loadMore = false,
                                    nextPage = result.paging?.next,
                                    isLoadingMore = false,
                                    isLoading = false
                                )
                            }
                        } else {
                            mutableUiState.update {
                                it.copy(
                                    loadMore = false,
                                    isLoadingMore = false,
                                    isLoading = false,
                                    message = result.message ?: result.error
                                )
                            }
                        }
                    } else {
                        try {
                            val cloudItems = supabaseRepository.getSavedMedia(uiState.mediaType.name)

                            val currentStatusName = uiState.listStatus?.name
                            val filteredItems = cloudItems.filter { item ->
                                isStatusMatching(currentStatusName, item.status)
                            }

                            // 🚀 FIXED: Save the filtered items directly into the UI state here too!
                            mutableUiState.update {
                                it.copy(
                                    cloudMediaList = filteredItems,
                                    loadMore = false,
                                    nextPage = null,
                                    isLoadingMore = false,
                                    isLoading = false
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mutableUiState.update {
                                it.copy(loadMore = false, isLoadingMore = false, isLoading = false)
                            }
                        }
                    }
                }
        }

        loadMediaForCurrentType()
    }
}