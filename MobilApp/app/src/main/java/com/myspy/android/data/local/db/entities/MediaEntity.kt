package com.myspy.android.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_files")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaType: String, // PHOTO, VIDEO, AUDIO
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String?,
    val width: Int?,
    val height: Int?,
    val duration: Long?, // For video/audio
    val timestamp: Long,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
