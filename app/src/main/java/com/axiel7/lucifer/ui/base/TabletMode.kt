package com.axiel7.lucifer.ui.base

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.lucifer.R
import com.axiel7.lucifer.data.model.base.Localizable

enum class TabletMode : Localizable {
    AUTO,
    ALWAYS,
    LANDSCAPE,
    NEVER;

    @get:StringRes
    val stringRes
        get() = when (this) {
            AUTO -> R.string.mode_auto
            ALWAYS -> R.string.mode_always
            LANDSCAPE -> R.string.mode_landscape
            NEVER -> R.string.mode_never
        }

    @Composable
    override fun localized() = stringResource(stringRes)

    companion object {
        val entriesLocalized = entries.associateWith { it.stringRes }
    }
}