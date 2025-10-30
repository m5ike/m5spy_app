package com.myspy.android.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // INCOMING, OUTGOING, MISSED
    val number: String,
    val duration: Int,
    val timestamp: Long,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
