package com.axiel7.lucifer.data.model.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- TMDB Models (Movies & Series) ---
@Serializable
data class TmdbResponse(val results: List<TmdbItem>)

@Serializable
data class TmdbItem(
    val id: Long,
    val title: String? = null,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    val overview: String? = null,
    // 🚀 NEW: Full Details Fields
    val genres: List<TmdbGenre>? = null,
    val runtime: Int? = null,
    @SerialName("episode_run_time") val episodeRunTime: List<Int>? = null,
    val status: String? = null,
    @SerialName("production_companies") val productionCompanies: List<TmdbCompany>? = null
)

@Serializable
data class TmdbGenre(val id: Int, val name: String)

@Serializable
data class TmdbCompany(val id: Int, val name: String)

// --- RAWG Models (Games) ---
@Serializable
data class RawgResponse(val results: List<RawgItem>)

@Serializable
data class RawgItem(
    val id: Long,
    val name: String,
    @SerialName("background_image") val backgroundImage: String? = null,
    val rating: Double? = null,
    val released: String? = null,
    val description_raw: String? = null,
    // 🚀 NEW: Full Details Fields
    val genres: List<RawgGenre>? = null,
    val playtime: Int? = null,
    val developers: List<RawgDeveloper>? = null
)

@Serializable
data class RawgGenre(val id: Int, val name: String)

@Serializable
data class RawgDeveloper(val id: Int, val name: String)