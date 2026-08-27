package com.axiel7.lucifer.ui.season

import com.axiel7.lucifer.data.model.anime.Season
import com.axiel7.lucifer.data.model.anime.SeasonType
import com.axiel7.lucifer.data.model.media.MediaSort
import com.axiel7.lucifer.ui.base.event.PagedUiEvent

interface SeasonChartEvent : PagedUiEvent {
    fun setSeason(season: Season? = null, year: Int? = null)
    fun setSeason(type: SeasonType)
    fun onChangeSort(value: MediaSort)
    fun onChangeIsNew(value: Boolean)
    fun onApplyFilters()
}