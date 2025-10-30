package com.myspy.android.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val width: Int,
    val height: Int,
    val timestamp: Long,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
