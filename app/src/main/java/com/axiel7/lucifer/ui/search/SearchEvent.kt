package com.axiel7.lucifer.ui.search

import com.axiel7.lucifer.data.model.SearchHistory
import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.event.PagedUiEvent

interface SearchEvent : PagedUiEvent {
    fun search(query: String)
    fun onChangeMediaType(value: MediaType)
    fun onSaveSearchHistory(query: String)
    fun onRemoveSearchHistory(item: SearchHistory)
    fun onAddToLibrary(media: CloudMedia) // 🚀 Added action to save to Supabase
}