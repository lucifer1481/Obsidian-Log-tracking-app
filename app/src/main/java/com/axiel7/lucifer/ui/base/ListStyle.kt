package com.axiel7.lucifer.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.base.Localizable

enum class ListStyle : Localizable {
    STANDARD,
    COMPACT,
    MINIMAL,
    GRID;

    val stringRes
        get() = when (this) {
            STANDARD -> R.string.list_mode_standard
            COMPACT -> R.string.list_mode_compact
            MINIMAL -> R.string.list_mode_minimal
            GRID -> R.string.list_mode_grid
        }

    @Composable
    override fun localized() = stringResource(stringRes)

    companion object {
        fun valueOfOrNull(value: String) = try {
            valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }

        val entriesLocalized = entries.associateWith { it.stringRes }
    }
}