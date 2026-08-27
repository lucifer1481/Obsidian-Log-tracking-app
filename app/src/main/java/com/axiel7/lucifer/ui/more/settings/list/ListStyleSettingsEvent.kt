package com.axiel7.lucifer.ui.more.settings.list

import com.axiel7.lucifer.data.model.media.ListStatus
import com.axiel7.lucifer.data.model.media.MediaType
import com.axiel7.lucifer.ui.base.ListStyle
import kotlinx.coroutines.flow.StateFlow

interface ListStyleSettingsEvent {
    fun getListStyle(mediaType: MediaType, status: ListStatus): StateFlow<ListStyle?>
    fun setListStyle(mediaType: MediaType, status: ListStatus, value: ListStyle)
}