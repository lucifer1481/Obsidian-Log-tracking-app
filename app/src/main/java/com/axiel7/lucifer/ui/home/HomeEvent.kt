package com.axiel7.lucifer.ui.home

import com.axiel7.lucifer.ui.base.event.UiEvent

interface HomeEvent : UiEvent {
    fun initRequestChain(isLoggedIn: Boolean)
}