package com.axiel7.lucifer.ui.custommedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomMediaUiState(
    val mediaList: List<CloudMedia> = emptyList(),
    val isLoading: Boolean = false,
    val selectedStatus: String = "ALL" // "ALL", "WATCHING", "COMPLETED", "PLAN_TO_WATCH"
)

class CustomMediaViewModel(
    private val supabaseRepository: SupabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomMediaUiState())
    val uiState: StateFlow<CustomMediaUiState> = _uiState.asStateFlow()

    fun loadMedia(mediaType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val list = supabaseRepository.getSavedMedia(mediaType)
            _uiState.value = _uiState.value.copy(
                mediaList = list,
                isLoading = false
            )
        }
    }

    fun onStatusFilterChanged(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
    }

    fun deleteItem(id: Long, mediaType: String) {
        viewModelScope.launch {
            supabaseRepository.deleteMedia(id)
            loadMedia(mediaType)
        }
    }
}