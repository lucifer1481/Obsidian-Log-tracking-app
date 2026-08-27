package com.axiel7.lucifer.data.model.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MainPicture(
    @SerialName("medium")
    val medium: String? = null,
    @SerialName("large")
    val large: String? = null
)