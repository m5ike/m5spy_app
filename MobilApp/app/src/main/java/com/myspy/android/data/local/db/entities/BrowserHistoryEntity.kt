package com.myspy.android.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browser_history")
data class BrowserHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String?,
    val visitCount: Int = 1,
    val timestamp: Long,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
