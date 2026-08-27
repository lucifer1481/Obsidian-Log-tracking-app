package com.axiel7.lucifer.data.model

import java.time.LocalDateTime

data class SearchHistory(
    val keyword: String,
    val updatedAt: LocalDateTime,
)
