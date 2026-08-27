package com.axiel7.lucifer.data.model.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ranking(
    @SerialName("rank")
    val rank: Int
)

