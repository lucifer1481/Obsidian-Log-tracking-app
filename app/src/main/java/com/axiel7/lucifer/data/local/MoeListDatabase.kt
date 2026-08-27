package com.axiel7.lucifer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axiel7.lucifer.data.local.searchhistory.SearchHistoryDao
import com.axiel7.lucifer.data.local.searchhistory.SearchHistoryEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
    ],
    version = 1,
)
@TypeConverters(DatabaseConverters::class)
abstract class MoeListDatabase : RoomDatabase() {
    abstract fun searchHistoryDao() : SearchHistoryDao
}
