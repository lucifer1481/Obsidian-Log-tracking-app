package com.axiel7.lucifer.data.model.manga

import com.axiel7.lucifer.data.model.media.BaseMediaList
import kotlinx.serialization.Serializable

@Serializable
data class MangaList(
    override val node: MangaNode
) : BaseMediaList

