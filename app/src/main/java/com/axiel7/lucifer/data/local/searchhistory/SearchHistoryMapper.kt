package com.axiel7.lucifer.data.local.searchhistory

import com.axiel7.lucifer.data.model.SearchHistory

fun SearchHistoryEntity.toSearchHistory(): SearchHistory {
    return SearchHistory(
        keyword = keyword,
        updatedAt = updatedAt,
    )
}

fun SearchHistory.toSearchEntity(): SearchHistoryEntity {
    return SearchHistoryEntity(
        keyword = keyword,
        updatedAt = updatedAt,
    )
}

fun List<SearchHistoryEntity>.toSearchHistoryList(): List<SearchHistory> {
    return map(SearchHistoryEntity::toSearchHistory)
}

fun List<SearchHistory>.toSearchHistoryEntityList(): List<SearchHistoryEntity> {
    return map(SearchHistory::toSearchEntity)
}
