package com.axiel7.lucifer.data.model.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudMedia(
    val id: Long? = null,
    @SerialName("api_id") val apiId: Long,
    @SerialName("media_type") val mediaType: String, // "MOVIES", "SERIES", "GAMES"
    val title: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val score: Int? = 0,     // 🚀 FIXED: Added '?' to allow NULLs from Supabase
    val progress: Int? = 0,  // 🚀 FIXED: Added '?' to allow NULLs
    val status: String? = "PLAN_TO_WATCH", // 🚀 FIXED: Added '?' just in case
)