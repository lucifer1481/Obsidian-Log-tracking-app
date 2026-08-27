package com.axiel7.lucifer.data.repository

import com.axiel7.lucifer.data.model.custom.RawgItem
import com.axiel7.lucifer.data.model.custom.RawgResponse
import com.axiel7.lucifer.data.model.custom.TmdbItem
import com.axiel7.lucifer.data.model.custom.TmdbResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ExternalMediaRepository {

    private val tmdbKey = "f3bac8896c45804479b31227ffbf3aa9"
    private val rawgKey = "56eeaa26dfd14410a3d7ad700572d0ab"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // --- Search Lists ---
    suspend fun searchMovies(query: String): TmdbResponse? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.themoviedb.org/3/search/movie") {
                parameter("api_key", tmdbKey)
                parameter("query", query)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchSeries(query: String): TmdbResponse? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.themoviedb.org/3/search/tv") {
                parameter("api_key", tmdbKey)
                parameter("query", query)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchGames(query: String): RawgResponse? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.rawg.io/api/games") {
                parameter("key", rawgKey)
                parameter("search", query)
                parameter("search_precise", true)
                parameter("search_exact", true)
                parameter("ordering", "-added")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Fetch Full Details ---
    suspend fun getMovieDetails(id: Int): TmdbItem? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.themoviedb.org/3/movie/$id") {
                parameter("api_key", tmdbKey)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSeriesDetails(id: Int): TmdbItem? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.themoviedb.org/3/tv/$id") {
                parameter("api_key", tmdbKey)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getGameDetails(id: Int): RawgItem? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.rawg.io/api/games/$id") {
                parameter("key", rawgKey)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🚀 NEW: Explore Page Endpoints
    // 🚀 UPDATED: Explore Page Endpoints (Now with pagination!)
    suspend fun getTrendingMovies(page: Int = 1): TmdbResponse? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.themoviedb.org/3/trending/movie/week") {
                parameter("api_key", tmdbKey)
                parameter("page", page) // Added page parameter
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPopularSeries(page: Int = 1): TmdbResponse? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.themoviedb.org/3/tv/popular") {
                parameter("api_key", tmdbKey)
                parameter("page", page) // Added page parameter
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getTrendingGames(page: Int = 1): RawgResponse? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.rawg.io/api/games") {
                parameter("key", rawgKey)
                // 🚀 FIXED: Sorts by most popular, but forces them to be top-rated AAA masterpieces
                parameter("ordering", "-added")
                parameter("metacritic", "85,100")
                parameter("page_size", 20)
                parameter("page", page)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}