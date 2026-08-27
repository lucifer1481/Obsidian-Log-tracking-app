package com.axiel7.moelist.data.model.media

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.Serializable

@Serializable
enum class MediaType : Localizable {
    ANIME, MANGA, MOVIES, SERIES, GAMES; // 🚀 Added the new types here!

    @Composable
    override fun localized() = when (this) {
        ANIME -> stringResource(R.string.anime)
        MANGA -> stringResource(R.string.manga)
        MOVIES -> stringResource(R.string.movies) // 🚀 Added localization
        SERIES -> stringResource(R.string.series) // 🚀 Added localization
        GAMES -> stringResource(R.string.games)   // 🚀 Added localization
    }

    companion object {
        // A helper list to easily show all types in our Search Bar
        val searchValues = listOf(ANIME, MANGA, MOVIES, SERIES, GAMES)

        val navType = object : NavType<MediaType>(isNullableAllowed = true) {
            override fun get(bundle: Bundle, key: String): MediaType? {
                return try {
                    bundle.getString(key)?.let {
                        MediaType.valueOf(it)
                    }
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            override fun parseValue(value: String): MediaType {
                return try {
                    MediaType.valueOf(value)
                } catch (_: IllegalArgumentException) {
                    ANIME
                }
            }

            override fun put(bundle: Bundle, key: String, value: MediaType) {
                bundle.putString(key, value.name)
            }
        }
    }
}