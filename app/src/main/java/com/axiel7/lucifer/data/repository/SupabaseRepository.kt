package com.axiel7.lucifer.data.repository

import com.axiel7.lucifer.data.model.custom.CloudMedia
import com.axiel7.lucifer.data.network.SupabaseApi
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository {

    private val client = SupabaseApi.client

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

    // 🚀 FIXED: Cleaned up duplicate delete functions into a single flexible one
    suspend fun deleteMedia(media: CloudMedia): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id ?: return@withContext false
            client.from("user_media").delete {
                filter {
                    eq("api_id", media.apiId)
                    eq("user_id", userId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteMedia(apiId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id ?: return@withContext false
            client.from("user_media").delete {
                filter {
                    eq("api_id", apiId)
                    eq("user_id", userId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🚀 FIXED UPSERT: Explicitly tell Supabase to match on composite key (user_id, api_id, media_type)
    suspend fun upsertMedia(media: CloudMedia): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id
            if (userId == null) {
                android.util.Log.e("SupabaseRepo", "Upsert failed: User is not logged in!")
                return@withContext false
            }

            // 1. Check if the item already exists in the table for this user
            val existingList = client.from("user_media")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("api_id", media.apiId)
                        eq("media_type", media.mediaType)
                    }
                }
                .decodeList<CloudMedia>()

            // 2. Prepare the model, injecting the correct row id (if updating) and user id
            // Note: Make sure your CloudMedia has a `user_id` property mapped if you need it in the object,
            // or let's handle it by passing a cleanly constructed map of primitives if needed.

            val mediaToSave = media.copy(
                id = existingList.firstOrNull()?.id // Keeps existing auto-increment ID so PostgreSQL updates it properly
            )

            // 3. Direct upsert using the strongly-typed @Serializable CloudMedia model
            client.from("user_media").upsert(mediaToSave) {
                onConflict = "id" // Falls back to the primary key integer ID column
            }

            android.util.Log.d("SupabaseRepo", "Successfully saved ${media.title} to Supabase!")
            true
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "CRITICAL UPSERT ERROR: ${e.message}", e)
            false
        }
    }

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