package com.axiel7.lucifer.ui.more.notifications

import com.axiel7.lucifer.ui.base.event.UiEvent

interface NotificationsEvent : UiEvent {
    fun removeNotification(animeId: Int)
    fun removeAllNotifications()
}