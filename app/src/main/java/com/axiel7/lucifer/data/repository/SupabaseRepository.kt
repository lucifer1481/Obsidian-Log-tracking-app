package com.axiel7.lucifer.data.repository

import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.network.SupabaseApi
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository {

    private val client = SupabaseApi.client

    // 🚀 FIXED: Using exact "media_type" column name from your Supabase table
    suspend fun getSavedMedia(mediaType: String): List<CloudMedia> = withContext(Dispatchers.IO) {
        try {
            client.from("user_media")
                .select {
                    filter {
                        eq("media_type", mediaType)
                    }
                }
                .decodeList<CloudMedia>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Insert or update an entry (Fixed with user_id attachment and corrected property names)
    suspend fun upsertMedia(media: CloudMedia): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id ?: return@withContext false

            // Map out the fields explicitly, matching your Supabase columns and CloudMedia properties
            val dataMap = mapOf(
                "user_id" to userId,
                "api_id" to media.apiId,
                "media_type" to media.mediaType,
                "title" to media.title,
                "image_url" to media.imageUrl, // 🚀 FIXED: Changed "poster" to "image_url" to match CloudMedia
                "score" to media.score,
                "progress" to media.progress,
                "status" to media.status
            )

            client.from("user_media").upsert(dataMap)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Delete an entry from the library
    suspend fun deleteMedia(apiId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("user_media").delete {
                filter {
                    eq("api_id", apiId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Checks if the item is already in your library
    suspend fun isMediaSaved(apiId: Long, mediaType: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val list = client.from("user_media")
                .select {
                    filter {
                        eq("api_id", apiId)
                        eq("media_type", mediaType)
                    }
                }
                .decodeList<CloudMedia>()
            list.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}